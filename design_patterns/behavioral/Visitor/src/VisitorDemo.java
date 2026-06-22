/**
 * <h1>Visitor — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Representar uma operação a ser realizada nos elementos de uma estrutura de objetos.
 * O Visitor permite definir uma nova operação sem mudar as classes dos elementos sobre
 * os quais opera.
 *
 * <h2>Double Dispatch</h2>
 * <p>Java é single dispatch — métodos são resolvidos pelo tipo do objeto receptor.
 * O Visitor implementa double dispatch: o método chamado depende do tipo do Visitor
 * <em>e</em> do tipo do Element.
 * <pre>
 *   element.accept(visitor)  → chama visitor.visit(this)  (dispatch 1: tipo do element)
 *   visitor.visit(element)   → sobrecarga correta          (dispatch 2: tipo do visitor)
 * </pre>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Adicionar operações a uma hierarquia de objetos sem modificá-la</li>
 *   <li>AST (abstract syntax trees) — compiladores, interpretadores</li>
 *   <li>Exportação de documentos em múltiplos formatos (HTML, PDF, Markdown)</li>
 *   <li>Quando a hierarquia de elementos é estável mas operações mudam frequentemente</li>
 * </ul>
 *
 * <h2>Tradeoff</h2>
 * <p>Adicionar novo tipo de Element exige modificar todos os Visitors.
 * Por isso, o Visitor assume hierarquia de Elements <em>estável</em> e
 * operações <em>variáveis</em>.
 *
 * <h2>Java 21 — alternativa com sealed + pattern matching</h2>
 * <p>Java 21 permite substituir o Visitor clássico com switch exaustivo sobre
 * sealed interface. Mais simples, menos boilerplate — demonstrado ao final.
 */
public class VisitorDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // ELEMENTS — nós de documento (hierarquia estável)
    // ─────────────────────────────────────────────────────────────────────────

    interface DocumentElement {
        void accept(DocumentVisitor visitor);
    }

    record Heading(int level, String text) implements DocumentElement {
        public void accept(DocumentVisitor v) { v.visit(this); }
    }

    record Paragraph(String text) implements DocumentElement {
        public void accept(DocumentVisitor v) { v.visit(this); }
    }

    record CodeBlock(String language, String code) implements DocumentElement {
        public void accept(DocumentVisitor v) { v.visit(this); }
    }

    record Link(String text, String url) implements DocumentElement {
        public void accept(DocumentVisitor v) { v.visit(this); }
    }

    record Image(String altText, String src) implements DocumentElement {
        public void accept(DocumentVisitor v) { v.visit(this); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VISITOR — interface com sobrecarga para cada tipo de elemento
    // ─────────────────────────────────────────────────────────────────────────

    interface DocumentVisitor {
        void visit(Heading heading);
        void visit(Paragraph paragraph);
        void visit(CodeBlock codeBlock);
        void visit(Link link);
        void visit(Image image);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCRETE VISITORS — novas operações sem modificar os Elements
    // ─────────────────────────────────────────────────────────────────────────

    /** Visitor 1 — exporta para HTML */
    static class HtmlExporter implements DocumentVisitor {
        private final StringBuilder sb = new StringBuilder();

        public void visit(Heading h) {
            sb.append("<h%d>%s</h%d>%n".formatted(h.level(), h.text(), h.level()));
        }
        public void visit(Paragraph p) {
            sb.append("<p>%s</p>%n".formatted(p.text()));
        }
        public void visit(CodeBlock c) {
            sb.append("<pre><code class=\"%s\">%s</code></pre>%n".formatted(c.language(), c.code()));
        }
        public void visit(Link l) {
            sb.append("<a href=\"%s\">%s</a>%n".formatted(l.url(), l.text()));
        }
        public void visit(Image i) {
            sb.append("<img src=\"%s\" alt=\"%s\"/>%n".formatted(i.src(), i.altText()));
        }
        String result() { return sb.toString(); }
    }

    /** Visitor 2 — exporta para Markdown */
    static class MarkdownExporter implements DocumentVisitor {
        private final StringBuilder sb = new StringBuilder();

        public void visit(Heading h) {
            sb.append("#".repeat(h.level())).append(" ").append(h.text()).append("\n");
        }
        public void visit(Paragraph p) {
            sb.append(p.text()).append("\n\n");
        }
        public void visit(CodeBlock c) {
            sb.append("```%s%n%s%n```%n".formatted(c.language(), c.code()));
        }
        public void visit(Link l) {
            sb.append("[%s](%s)".formatted(l.text(), l.url()));
        }
        public void visit(Image i) {
            sb.append("![%s](%s)%n".formatted(i.altText(), i.src()));
        }
        String result() { return sb.toString(); }
    }

    /** Visitor 3 — extrai estatísticas (sem renderização) */
    static class StatsCollector implements DocumentVisitor {
        int headings, paragraphs, codeBlocks, links, images, wordCount;

        public void visit(Heading h) { headings++; wordCount += countWords(h.text()); }
        public void visit(Paragraph p) { paragraphs++; wordCount += countWords(p.text()); }
        public void visit(CodeBlock c) { codeBlocks++; }
        public void visit(Link l) { links++; }
        public void visit(Image i) { images++; }

        private int countWords(String text) { return text.isBlank() ? 0 : text.split("\\s+").length; }

        void print() {
            System.out.printf("  Headings:%2d | Paragraphs:%2d | Code:%2d | Links:%2d | Images:%2d | Words:%3d%n",
                    headings, paragraphs, codeBlocks, links, images, wordCount);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Java 21 — Visitor via sealed + pattern matching (alternativa moderna)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Com sealed interface e pattern matching for switch, o Visitor clássico
     * pode ser substituído por uma função pura — sem a cerimônia do double dispatch.
     * O compilador ainda garante exaustividade.
     */
    sealed interface DocNode permits DocNode.H, DocNode.P, DocNode.Code {
        record H(int level, String text) implements DocNode {}
        record P(String text)            implements DocNode {}
        record Code(String lang, String src) implements DocNode {}
    }

    static String renderHtml(DocNode node) {
        return switch (node) {
            case DocNode.H(var lvl, var txt) -> "<h%d>%s</h%d>".formatted(lvl, txt, lvl);
            case DocNode.P(var txt)          -> "<p>%s</p>".formatted(txt);
            case DocNode.Code(var lang, var src) -> "<pre><code class=\"%s\">%s</code></pre>".formatted(lang, src);
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Visitor Pattern — Document Export ===\n");

        // Documento de exemplo
        var elements = new DocumentElement[]{
                new Heading(1, "Java Design Patterns"),
                new Paragraph("Padrões de projeto são soluções reutilizáveis para problemas comuns."),
                new CodeBlock("java", "record Point(int x, int y) {}"),
                new Link("Refactoring Guru", "https://refactoring.guru"),
                new Image("Logo Java", "/img/java-logo.png"),
                new Heading(2, "Visitor Pattern"),
                new Paragraph("O Visitor permite adicionar operações sem modificar os elementos.")
        };

        // Aplicar diferentes visitors — sem modificar os elementos
        var html  = new HtmlExporter();
        var md    = new MarkdownExporter();
        var stats = new StatsCollector();

        for (var el : elements) {
            el.accept(html);
            el.accept(md);
            el.accept(stats);
        }

        System.out.println("--- HTML ---");
        System.out.println(html.result());

        System.out.println("--- Markdown ---");
        System.out.println(md.result());

        System.out.println("--- Estatísticas ---");
        stats.print();

        // Java 21 — alternativa com sealed
        System.out.println("--- Java 21: sealed + switch (sem boilerplate) ---");
        var nodes = new DocNode[]{
                new DocNode.H(1, "Título"),
                new DocNode.P("Parágrafo de exemplo"),
                new DocNode.Code("java", "System.out.println(\"Hello\");")
        };
        for (var node : nodes) {
            System.out.println("  " + renderHtml(node));
        }
    }
}
