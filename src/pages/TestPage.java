package src.pages;

import src.components.fields.InputField;
import src.pages.cores.Page;

public class TestPage extends Page {
    InputField inputField = new InputField(20, "Type something...");
    // MemoField memoField = new MemoField(20, 30, "Type something...", false);

    public void select()
    {

    }

    public void render(StringBuilder frame)
    {
        inputField.render(frame);
        // memoField.render();
    }

    public void handleAction(String action)
    {
        inputField.handleInput(action);
        // memoField.handleInput(action);
    }
}
