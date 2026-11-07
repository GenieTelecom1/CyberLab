package com.cyberlab.util;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Transforme le contenu Markdown d'un cours en page HTML complete (theme sombre),
 * affichee dans un WebView. Gere les tableaux, les listes a cocher et les images.
 */
public final class MarkdownRenderer {

    private static final List<Extension> EXTENSIONS =
            List.of(TablesExtension.create(), TaskListItemsExtension.create());

    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    private static final String CSS = """
            body { background:#0f172a; color:#e2e8f0; font-family:'Segoe UI',Arial,sans-serif;
                   font-size:15px; line-height:1.65; margin:0; padding:24px 32px; }
            h1 { color:#f8fafc; border-bottom:2px solid #38bdf8; padding-bottom:6px; margin-top:34px; }
            h2 { color:#7dd3fc; margin-top:28px; }
            h3 { color:#fde68a; margin-top:22px; }
            a { color:#60a5fa; }
            strong { color:#f8fafc; }
            code { background:#1e293b; color:#fda4af; padding:1px 5px; border-radius:4px; font-family:Consolas,monospace; }
            blockquote { background:#1e293b; border-left:4px solid #38bdf8; margin:14px 0;
                         padding:8px 16px; border-radius:6px; }
            blockquote p { margin:6px 0; }
            table { border-collapse:collapse; width:100%; margin:14px 0; }
            th { background:#1e3a8a; color:#dbeafe; text-align:left; }
            th, td { border:1px solid #334155; padding:8px 10px; vertical-align:top; }
            tr:nth-child(even) td { background:#1e293b; }
            img { max-width:100%; height:auto; border-radius:10px; margin:12px 0; display:block; }
            hr { border:0; border-top:1px solid #334155; margin:28px 0; }
            ul, ol { padding-left:26px; }
            li.task { list-style:none; margin-left:-22px; cursor:pointer; padding:2px 0; }
            li.task input[type=checkbox] { width:16px; height:16px; margin-right:10px; cursor:pointer;
                                           vertical-align:middle; }
            li.task.done { color:#86efac; text-decoration:line-through; }
            """;

    /** Rend les cases a cocher cliquables (clic sur la case ou sur le texte) et barre la ligne cochee. */
    private static final String SCRIPT = """
            document.querySelectorAll('li.task').forEach(function (li) {
                var cb = li.querySelector('input[type=checkbox]');
                if (!cb) { return; }
                function sync() { li.classList.toggle('done', cb.checked); }
                cb.addEventListener('change', sync);
                li.addEventListener('click', function (e) {
                    if (e.target !== cb) { cb.checked = !cb.checked; sync(); }
                });
                sync();
            });
            """;

    private MarkdownRenderer() {
    }

    public static String toHtml(String markdown) {
        String md = markdown == null ? "" : markdown;
        Node document = PARSER.parse(md);
        String body = RENDERER.render(document);
        // commonmark genere les cases "disabled" (lecture seule) : on les rend cliquables.
        body = body.replaceAll("(<input type=\"checkbox\"[^>]*?)\\s+disabled(=\"[^\"]*\")?", "$1");
        body = body.replaceAll("<li>(\\s*<input type=\"checkbox\")", "<li class=\"task\">$1");
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>" + CSS
                + "</style></head><body>" + body + "<script>" + SCRIPT + "</script></body></html>";
    }

    /** Page affichee quand aucun cours n'est selectionne ou en cas d'erreur. */
    public static String message(String text) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>" + CSS
                + "</style></head><body><p>" + safe + "</p></body></html>";
    }
}