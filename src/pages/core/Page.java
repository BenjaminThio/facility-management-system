package src.pages.core;

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
    public abstract void render();
    public abstract void select();
    public abstract void handleAction(String action);
}
