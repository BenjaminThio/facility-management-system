package src.pages;

import java.util.ArrayList;
import java.util.Arrays;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Table;
import src.models.Notification;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;

public class NotificationPage extends Page {
    private static final int PAGE_WIDTH = 95;

    public NotificationPage() {
        this.selection = 0;
    }

    @Override
    public void render(StringBuilder frame) {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();
        ArrayList<Notification> inbox = Global.getUser().getNotifications();

        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder().append(new Container(" Notification Center ", PAGE_WIDTH, Container.Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi())
        )));

        AnsiBuilder content = new AnsiBuilder();
        content.append(new Container("", PAGE_WIDTH, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");

        if (inbox.isEmpty()) {
            content.append(new Container(" You have no notifications. ", PAGE_WIDTH, Container.Alignment.CENTER, Ansi.BG_WHITE, Ansi.FG_DARK_GRAY).toAnsi()).append("\n");
            content.append(new Container("", PAGE_WIDTH, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi());
        } else {
            for (Notification n : inbox) {
                int bgColor = Ansi.BG_WHITE;
                int fgColor = Ansi.FG_BLACK;

                if (n.isRead()) {
                    fgColor = Ansi.FG_DARK_GRAY;
                } else if (n.getContent().contains("ALERT")) {
                    bgColor = Ansi.BG_RED;
                    fgColor = Ansi.FG_WHITE;
                } else if (n.getContent().contains("ASSIGNMENT")) {
                    bgColor = Ansi.BG_YELLOW;
                } else if (n.getContent().contains("APPROVED")) {
                    bgColor = Ansi.BG_LIGHT_GREEN;
                }

                String prefix = n.isRead() ? "      " : " NEW! ";
                String displayStr = String.format("%s [%s] %s", prefix, n.getFrom(), n.getContent());

                content.append(new Container(displayStr, PAGE_WIDTH, Container.Alignment.LEFT, bgColor, fgColor).toAnsi()).append("\n");
                content.append(new Container(String.format("        %s", n.getTimestamp()), PAGE_WIDTH, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_DARK_GRAY).toAnsi()).append("\n");
                content.append(new Container("", PAGE_WIDTH, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n"); 
            }
        }

        table.add(new ArrayList<>(Arrays.asList(content)));

        Table.render(frame, table);
    }

    @Override
    public void handleAction(String action) {
        if (action.equals("ESC")) {
            for (Notification n : Global.getUser().getNotifications()) {
                n.setRead(true);
            }
            Database.User.save();
            Router.back();
        }
        Renderer.refresh();
    }
}