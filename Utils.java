public class Utils {
    public static String toTitleCase(String s)
    {
        return toTitleCase(s, "_");
    }

    public static String toTitleCase(String s, String delimiter)
    {
        String result = "";

        for (String w : s.split(delimiter))
        {
            int counter = 0;

            for (char c : w.toCharArray())
            {
                counter++;

                switch (counter)
                {
                    case 1:
                        result += Character.toUpperCase(c);
                        break;
                    default:
                        result += Character.toLowerCase(c);
                        break;
                }
            }

            counter = 0;
            result += ' ';
        }

        return result;
    }
}
