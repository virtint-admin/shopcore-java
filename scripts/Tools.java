/*
 * Build helpers for the ShopCore Makefile. Runs with the JDK source launcher, no dependencies:
 *
 *   java scripts/Tools.java junit-status  <junit-dir> <status.json> <phase>
 *   java scripts/Tools.java status        <status.json> <phase> <result>
 *   java scripts/Tools.java cobertura     <jacoco.xml> <out.xml>
 *   java scripts/Tools.java coverage-min  <cobertura.xml> <min-line-rate>
 *   java scripts/Tools.java lint          <checkstyle.xml> <spotless.log> <spotless-exit> <out.json>
 *   java scripts/Tools.java functions     <repo-dir> <out.json>
 *   java scripts/Tools.java functions-check <repo-dir> <functions.json>
 *
 * Exit codes (read by the Makefile and the review workflow): junit-status returns 0 (all green),
 * 1 (a test failed) or 2 (no results, infra error).
 */

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Tools {

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      usage();
    }
    int rc =
        switch (args[0]) {
          case "junit-status" -> junitStatus(Path.of(args[1]), Path.of(args[2]), args[3]);
          case "status" -> writeStatus(Path.of(args[1]), args[2], args[3], -1, -1);
          case "cobertura" -> cobertura(Path.of(args[1]), Path.of(args[2]));
          case "coverage-min" -> coverageMin(Path.of(args[1]), Double.parseDouble(args[2]));
          case "lint" -> lint(Path.of(args[1]), Path.of(args[2]), Integer.parseInt(args[3]), Path.of(args[4]));
          case "functions" -> functions(Path.of(args[1]), Path.of(args[2]), false);
          case "functions-check" -> functions(Path.of(args[1]), Path.of(args[2]), true);
          default -> {
            usage();
            yield 2;
          }
        };
    System.exit(rc);
  }

  private static void usage() {
    System.err.println(
        "usage: Tools junit-status|cobertura|coverage-min|lint|functions|functions-check ...");
    System.exit(2);
  }

  // ---------------------------------------------------------------- XML helpers

  private static Document parseXml(Path file) throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setValidating(false);
    f.setNamespaceAware(false);
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    f.setFeature("http://xml.org/sax/features/external-general-entities", false);
    f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    DocumentBuilder b = f.newDocumentBuilder();
    return b.parse(file.toFile());
  }

  private static List<Element> children(Element parent, String tag) {
    List<Element> out = new ArrayList<>();
    NodeList nl = parent.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      if (n instanceof Element e && e.getTagName().equals(tag)) {
        out.add(e);
      }
    }
    return out;
  }

  private static String xmlEscape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  private static String jsonString(String s) {
    StringBuilder sb = new StringBuilder("\"");
    for (char c : s.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.append('"').toString();
  }

  // ---------------------------------------------------------------- junit-status

  /** Writes reports/status.json; negative counts are omitted. */
  static int writeStatus(Path out, String phase, String result, int total, int failed)
      throws IOException {
    StringBuilder json = new StringBuilder("{\"phase\": ").append(jsonString(phase));
    json.append(", \"result\": ").append(jsonString(result));
    if (total >= 0) {
      json.append(", \"tests_total\": ").append(total);
      json.append(", \"tests_failed\": ").append(failed);
    }
    json.append("}\n");
    Files.createDirectories(out.toAbsolutePath().getParent());
    Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
    return 0;
  }

  static int junitStatus(Path dir, Path statusFile, String phase) throws Exception {
    int rc = junitStatus(dir);
    writeStatus(
        statusFile,
        phase,
        rc == 0 ? "pass" : rc == 1 ? "tests_failed" : "error",
        lastTotal,
        lastFailed);
    return rc;
  }

  private static int lastTotal;
  private static int lastFailed;

  static int junitStatus(Path dir) throws Exception {
    lastTotal = 0;
    lastFailed = 0;
    if (!Files.isDirectory(dir)) {
      System.err.println("junit-status: no report directory " + dir);
      return 2;
    }
    List<Path> files;
    try (Stream<Path> s = Files.list(dir)) {
      files = s.filter(p -> p.getFileName().toString().endsWith(".xml")).sorted().toList();
    }
    int total = 0;
    int skipped = 0;
    List<String> failed = new ArrayList<>();
    for (Path file : files) {
      Element root = parseXml(file).getDocumentElement();
      List<Element> suites =
          root.getTagName().equals("testsuites") ? children(root, "testsuite") : List.of(root);
      for (Element suite : suites) {
        for (Element tc : children(suite, "testcase")) {
          total++;
          String id = tc.getAttribute("classname") + "#" + tc.getAttribute("name");
          if (!children(tc, "failure").isEmpty() || !children(tc, "error").isEmpty()) {
            failed.add(id);
          } else if (!children(tc, "skipped").isEmpty()) {
            skipped++;
          }
        }
      }
    }
    lastTotal = total;
    lastFailed = failed.size();
    if (total == 0) {
      System.err.println("junit-status: no test results found in " + dir);
      return 2;
    }
    System.out.printf(
        Locale.ROOT,
        "tests: %d, passed: %d, failed: %d, skipped: %d%n",
        total,
        total - failed.size() - skipped,
        failed.size(),
        skipped);
    for (String id : failed) {
      System.out.println("FAILED " + id);
    }
    return failed.isEmpty() ? 0 : 1;
  }

  // ---------------------------------------------------------------- cobertura

  private static int intAttr(Element e, String name) {
    String v = e.getAttribute(name);
    return v.isEmpty() ? 0 : Integer.parseInt(v);
  }

  private static String rate(long covered, long valid) {
    return valid == 0 ? "1" : String.format(Locale.ROOT, "%.4f", (double) covered / valid);
  }

  static int cobertura(Path jacocoXml, Path out) throws Exception {
    Element report = parseXml(jacocoXml).getDocumentElement();
    long timestamp = 0;
    for (Element si : children(report, "sessioninfo")) {
      timestamp = Long.parseLong(si.getAttribute("start")) / 1000;
    }
    StringBuilder packagesXml = new StringBuilder();
    long allLines = 0;
    long allCovered = 0;
    long allBranches = 0;
    long allBranchesCovered = 0;
    for (Element pkg : children(report, "package")) {
      String pkgName = pkg.getAttribute("name").replace('/', '.');
      StringBuilder classesXml = new StringBuilder();
      long pLines = 0;
      long pCovered = 0;
      long pBranches = 0;
      long pBranchesCovered = 0;
      for (Element src : children(pkg, "sourcefile")) {
        String fileName = pkg.getAttribute("name") + "/" + src.getAttribute("name");
        String className = pkgName + "." + src.getAttribute("name").replaceFirst("\\.java$", "");
        StringBuilder linesXml = new StringBuilder();
        long lines = 0;
        long covered = 0;
        long branches = 0;
        long branchesCovered = 0;
        for (Element line : children(src, "line")) {
          int nr = intAttr(line, "nr");
          int ci = intAttr(line, "ci");
          int mb = intAttr(line, "mb");
          int cb = intAttr(line, "cb");
          lines++;
          if (ci > 0) {
            covered++;
          }
          linesXml.append("            <line number=\"").append(nr).append("\" hits=\"");
          linesXml.append(ci > 0 ? 1 : 0).append('"');
          if (mb + cb > 0) {
            branches += mb + cb;
            branchesCovered += cb;
            linesXml.append(" branch=\"true\" condition-coverage=\"");
            linesXml.append((int) Math.round(100.0 * cb / (mb + cb))).append("% (");
            linesXml.append(cb).append('/').append(mb + cb).append(")\"");
          } else {
            linesXml.append(" branch=\"false\"");
          }
          linesXml.append("/>\n");
        }
        classesXml
            .append("        <class name=\"")
            .append(xmlEscape(className))
            .append("\" filename=\"")
            .append(xmlEscape(fileName))
            .append("\" line-rate=\"")
            .append(rate(covered, lines))
            .append("\" branch-rate=\"")
            .append(rate(branchesCovered, branches))
            .append("\" complexity=\"0\">\n          <methods/>\n          <lines>\n")
            .append(linesXml)
            .append("          </lines>\n        </class>\n");
        pLines += lines;
        pCovered += covered;
        pBranches += branches;
        pBranchesCovered += branchesCovered;
      }
      packagesXml
          .append("    <package name=\"")
          .append(xmlEscape(pkgName))
          .append("\" line-rate=\"")
          .append(rate(pCovered, pLines))
          .append("\" branch-rate=\"")
          .append(rate(pBranchesCovered, pBranches))
          .append("\" complexity=\"0\">\n      <classes>\n")
          .append(classesXml)
          .append("      </classes>\n    </package>\n");
      allLines += pLines;
      allCovered += pCovered;
      allBranches += pBranches;
      allBranchesCovered += pBranchesCovered;
    }
    StringWriter w = new StringWriter();
    w.append("<?xml version=\"1.0\" ?>\n");
    w.append(
        "<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/coverage-04.dtd\">\n");
    w.append("<coverage line-rate=\"" + rate(allCovered, allLines) + "\"");
    w.append(" branch-rate=\"" + rate(allBranchesCovered, allBranches) + "\"");
    w.append(" lines-covered=\"" + allCovered + "\" lines-valid=\"" + allLines + "\"");
    w.append(" branches-covered=\"" + allBranchesCovered + "\"");
    w.append(" branches-valid=\"" + allBranches + "\"");
    w.append(" complexity=\"0\" version=\"jacoco-to-cobertura 1.0\"");
    w.append(" timestamp=\"" + timestamp + "\">\n");
    w.append("  <sources>\n    <source>src/main/java</source>\n  </sources>\n");
    w.append("  <packages>\n").append(packagesXml).append("  </packages>\n</coverage>\n");
    Files.createDirectories(out.toAbsolutePath().getParent());
    Files.writeString(out, w.toString(), StandardCharsets.UTF_8);
    System.out.printf(
        Locale.ROOT,
        "line coverage: %.2f%% (%d/%d lines), branch coverage: %.2f%%%n",
        allLines == 0 ? 100.0 : 100.0 * allCovered / allLines,
        allCovered,
        allLines,
        allBranches == 0 ? 100.0 : 100.0 * allBranchesCovered / allBranches);
    return 0;
  }

  static int coverageMin(Path cobertura, double min) throws Exception {
    double rate = Double.parseDouble(parseXml(cobertura).getDocumentElement().getAttribute("line-rate"));
    if (rate < min) {
      System.err.printf(Locale.ROOT, "line coverage %.4f is below %.4f%n", rate, min);
      return 1;
    }
    System.out.printf(Locale.ROOT, "line coverage %.4f >= %.4f%n", rate, min);
    return 0;
  }

  // ---------------------------------------------------------------- lint

  private record LintError(String path, int line, String code, String message) {}

  private static final Pattern SPOTLESS_FILE =
      Pattern.compile("^\\[ERROR\\]\\s+(src/\\S+\\.java)\\s*$");
  private static final Pattern SPOTLESS_HUNK =
      Pattern.compile("^\\[ERROR\\]\\s+@@ -(\\d+)");

  static int lint(Path checkstyleXml, Path spotlessLog, int spotlessExit, Path out)
      throws Exception {
    Path base = Path.of("").toAbsolutePath();
    List<LintError> errors = new ArrayList<>();
    if (!Files.exists(checkstyleXml)) {
      System.err.println("lint: checkstyle produced no report (" + checkstyleXml + ")");
      return 2;
    }
    Element root = parseXml(checkstyleXml).getDocumentElement();
    for (Element file : children(root, "file")) {
      Path p = Path.of(file.getAttribute("name"));
      String rel = (p.isAbsolute() ? base.relativize(p) : p).toString().replace('\\', '/');
      for (Element err : children(file, "error")) {
        String source = err.getAttribute("source");
        String code = source.substring(source.lastIndexOf('.') + 1).replaceFirst("Check$", "");
        errors.add(new LintError(rel, intAttr(err, "line"), code, err.getAttribute("message")));
      }
    }
    int spotlessFiles = 0;
    if (spotlessExit != 0) {
      String current = null;
      int firstHunk = 0;
      for (String line : Files.readAllLines(spotlessLog, StandardCharsets.UTF_8)) {
        Matcher fm = SPOTLESS_FILE.matcher(line);
        Matcher hm = SPOTLESS_HUNK.matcher(line);
        if (fm.matches()) {
          if (current != null) {
            errors.add(spotless(current, firstHunk));
            spotlessFiles++;
          }
          current = fm.group(1);
          firstHunk = 0;
        } else if (hm.find() && current != null && firstHunk == 0) {
          firstHunk = Integer.parseInt(hm.group(1));
        }
      }
      if (current != null) {
        errors.add(spotless(current, firstHunk));
        spotlessFiles++;
      }
      if (spotlessFiles == 0) {
        System.err.println("lint: spotless failed without reporting files; see " + spotlessLog);
        return 2;
      }
    }
    errors.removeIf(e -> !e.path().startsWith("src/main/"));
    errors.sort(
        (a, b) ->
            a.path().equals(b.path())
                ? Integer.compare(a.line(), b.line())
                : a.path().compareTo(b.path()));
    StringBuilder json = new StringBuilder("{\"tool\": \"checkstyle+spotless\", \"errors\": [");
    for (int i = 0; i < errors.size(); i++) {
      LintError e = errors.get(i);
      json.append(i == 0 ? "\n  " : ",\n  ");
      json.append("{\"path\": ").append(jsonString(e.path()));
      json.append(", \"line\": ").append(e.line());
      json.append(", \"code\": ").append(jsonString(e.code()));
      json.append(", \"message\": ").append(jsonString(e.message())).append('}');
    }
    json.append(errors.isEmpty() ? "]}\n" : "\n]}\n");
    Files.createDirectories(out.toAbsolutePath().getParent());
    Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
    System.out.println("lint: " + errors.size() + " error(s) -> " + out);
    return 0;
  }

  private static LintError spotless(String path, int line) {
    return new LintError(
        path,
        Math.max(1, line),
        "spotless",
        "File is not formatted with google-java-format; run `mvn spotless:apply`");
  }

  // ---------------------------------------------------------------- functions.json

  private record Fn(String file, int start, int end, String module) {}

  static int functions(Path repoArg, Path out, boolean check) throws Exception {
    Path repo = repoArg.toAbsolutePath().normalize();
    Path mainRoot = repo.resolve("src/main/java");
    Path testRoot = repo.resolve("src/test/java");
    List<String> problems = new ArrayList<>();
    Map<String, Fn> fns = new TreeMap<>();
    Map<String, List<String>> tests = new TreeMap<>();
    Set<String> testIds = new TreeSet<>();

    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fm = javac.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
      List<Path> sources = javaFiles(mainRoot);
      List<Path> testSources = javaFiles(testRoot);
      List<Path> all = new ArrayList<>(sources);
      all.addAll(testSources);
      Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromPaths(all);
      JavacTask task =
          (JavacTask) javac.getTask(null, fm, null, List.of("-proc:none"), null, units);
      SourcePositions pos = Trees.instance(task).getSourcePositions();
      for (CompilationUnitTree cu : task.parse()) {
        Path file = Path.of(cu.getSourceFile().toUri());
        boolean isTest = file.startsWith(testRoot);
        String pkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString();
        for (Tree t : cu.getTypeDecls()) {
          if (!(t instanceof ClassTree ct)) {
            continue;
          }
          String cls = pkg + "." + ct.getSimpleName();
          if (isTest) {
            collectTests(ct, cls, tests, testIds);
          } else if (ct.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
            collectFunctions(ct, cls, cu, pos, repo, fns, problems);
          }
        }
      }
    }

    StringBuilder json = new StringBuilder("{\n");
    int i = 0;
    Set<String> mappedTests = new TreeSet<>();
    for (Map.Entry<String, Fn> e : fns.entrySet()) {
      String fnId = e.getKey();
      String cls = fnId.substring(0, fnId.indexOf('#'));
      String method = fnId.substring(fnId.indexOf('#') + 1);
      List<String> ts = new ArrayList<>();
      for (String id : tests.getOrDefault(cls + "Test", List.of())) {
        String name = id.substring(id.indexOf('#') + 1);
        if (name.substring(0, name.indexOf('_')).equals(method)) {
          ts.add(id);
          mappedTests.add(id);
        }
      }
      if (ts.size() < 3) {
        problems.add(fnId + " has " + ts.size() + " test(s); at least 3 are required");
      }
      Fn fn = e.getValue();
      json.append(i++ == 0 ? "  " : ",\n  ").append(jsonString(fnId)).append(": {");
      json.append("\"module\": ").append(jsonString(fn.module()));
      json.append(", \"file\": ").append(jsonString(fn.file()));
      json.append(", \"start_line\": ").append(fn.start());
      json.append(", \"end_line\": ").append(fn.end());
      json.append(", \"tests\": [");
      for (int j = 0; j < ts.size(); j++) {
        json.append(j == 0 ? "" : ", ").append(jsonString(ts.get(j)));
      }
      json.append("]}");
    }
    json.append("\n}\n");
    for (String id : testIds) {
      if (!mappedTests.contains(id)) {
        problems.add("test " + id + " does not map to a public method (<method>_<case>)");
      }
    }
    for (String p : problems) {
      System.err.println("functions: " + p);
    }
    if (check) {
      String current = Files.exists(out) ? Files.readString(out, StandardCharsets.UTF_8) : "";
      if (!current.equals(json.toString())) {
        System.err.println("functions: " + out + " is stale; run `make functions`");
        return 1;
      }
    } else {
      Files.createDirectories(out.toAbsolutePath().getParent());
      Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
    }
    System.out.println(
        "functions: " + fns.size() + " public methods, " + mappedTests.size() + " mapped tests");
    return problems.isEmpty() ? 0 : 1;
  }

  private static List<Path> javaFiles(Path root) throws IOException {
    if (!Files.isDirectory(root)) {
      return List.of();
    }
    try (Stream<Path> s = Files.walk(root)) {
      return s.filter(p -> p.toString().endsWith(".java")).sorted().toList();
    }
  }

  private static void collectFunctions(
      ClassTree ct,
      String cls,
      CompilationUnitTree cu,
      SourcePositions pos,
      Path repo,
      Map<String, Fn> fns,
      List<String> problems) {
    String rel =
        repo.toAbsolutePath()
            .relativize(Path.of(cu.getSourceFile().toUri()))
            .toString()
            .replace('\\', '/');
    String[] parts = cls.split("\\.");
    String module = parts.length > 2 ? parts[2] : "";
    for (Tree member : ct.getMembers()) {
      if (member instanceof ClassTree nested
          && nested.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
        for (Tree m : nested.getMembers()) {
          if (m instanceof MethodTree mt && isCounted(mt, nested)) {
            problems.add(cls + "." + nested.getSimpleName() + "#" + mt.getName()
                + " is a public method on a nested type; move the type to its own file");
          }
        }
      }
      if (!(member instanceof MethodTree mt) || !isCounted(mt, ct)) {
        continue;
      }
      String id = cls + "#" + mt.getName();
      if (fns.containsKey(id)) {
        problems.add(id + " is overloaded; public methods must have unique names per class");
        continue;
      }
      long start = pos.getStartPosition(cu, mt);
      long end = pos.getEndPosition(cu, mt);
      int startLine = (int) cu.getLineMap().getLineNumber(start);
      int endLine = (int) cu.getLineMap().getLineNumber(end);
      fns.put(id, new Fn(rel, startLine, endLine, module));
    }
  }

  private static boolean isCounted(MethodTree mt, ClassTree owner) {
    if (mt.getName().contentEquals("<init>") || mt.getBody() == null) {
      return false;
    }
    Set<Modifier> flags = mt.getModifiers().getFlags();
    boolean isInterface = owner.getKind() == Tree.Kind.INTERFACE;
    return flags.contains(Modifier.PUBLIC) || (isInterface && !flags.contains(Modifier.PRIVATE));
  }

  private static void collectTests(
      ClassTree ct, String cls, Map<String, List<String>> tests, Set<String> testIds) {
    for (Tree member : ct.getMembers()) {
      if (!(member instanceof MethodTree mt)) {
        continue;
      }
      boolean isTest =
          mt.getModifiers().getAnnotations().stream()
              .anyMatch(a -> a.getAnnotationType().toString().equals("Test"));
      if (!isTest) {
        continue;
      }
      String id = cls + "#" + mt.getName();
      testIds.add(id);
      if (mt.getName().toString().indexOf('_') > 0) {
        tests.computeIfAbsent(cls, k -> new ArrayList<>()).add(id);
      }
    }
  }
}
