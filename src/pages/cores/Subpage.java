package src.pages.cores;

import java.util.ArrayList;
import java.util.List;

import src.utils.Router;

public abstract class Subpage extends Page {
    protected int id;
    protected String label;

    public class Route
    {
        private int id;
        private String label;

        public Route(int id, String label)
        {
            this.id = id;
            this.label = label;
        }

        public int getId()
        {
            return this.id;
        }

        public String getLabel()
        {
            return this.label;
        }

        @Override
        public String toString() {
            return String.format("(%d,%s)", id, label);
        }
    }

    public void init() {}

    public void setId(int id)
    {
        this.id = id;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public int getId()
    {
        return this.id;
    }

    public String getLabel()
    {
        return this.label;
    }

    public int[] getIdRoute()
    {
        int counter = Router.getRoutesHistory().size();
        List<Integer> ids = new ArrayList<>();

        while (counter - 1 >= 0 && Router.getRoutesHistory().get(counter - 1) instanceof Subpage subpage)
        {
            ids.add(0, subpage.getId());
            counter--;
        }

        ids.remove(0);

        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    public String[] getLabelRoute()
    {
        int counter = Router.getRoutesHistory().size();
        List<String> labels = new ArrayList<>();

        while (counter - 1 >= 0 && Router.getRoutesHistory().get(counter - 1) instanceof Subpage subpage)
        {
            labels.add(0, subpage.getLabel());
            counter--;
        }

        labels.remove(0);

        return labels.toArray(String[]::new);
    }

    public Route[] getRoute()
    {
        int counter = Router.getRoutesHistory().size();
        List<Route> identities = new ArrayList<>();

        while (counter - 1 >= 0 && Router.getRoutesHistory().get(counter - 1) instanceof Subpage subpage)
        {
            identities.add(0, new Route(subpage.getId(), subpage.getLabel()));
            counter--;
        }

        identities.remove(0);

        return identities.toArray(Route[]::new);
    }
}
