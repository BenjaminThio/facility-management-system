package src.pages.cores;

import src.utils.Renderer;

public abstract class Page {
    protected int selection = 0;

    public int getSelection()
    {
        return this.selection;
    }

    public void setSelection(int selection)
    {
        this.selection = selection;
    }

    // public abstract int getSelectionSize();
    public abstract void render(StringBuilder frame);
    public abstract void handleAction(String action);
    public void updateCaret()
    {
        Renderer.hideInputCaret();
    };
}
