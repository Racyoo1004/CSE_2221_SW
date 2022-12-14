import java.io.Serializable;
import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Methods to generate the glossary based on the input terms and definitions.
 *
 * @author Yoojin Jeong
 */
public final class Glossary {

    /**
     *
     * @date 11/24/2020
     */
    private static class StringLT implements Comparator<String>, Serializable {

        /**
         * Generated serial version UID for this inner class.
         */
        private static final long serialVersionUID = -5811319454240625460L;

        /**
         * Compares two string objects and reports the difference.
         *
         * @param o1
         *            the first string object
         * @param o2
         *            the second string object
         * @return the difference between the two string objects
         */
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Private constructor so that this class cannot be initialized.
     */
    private Glossary() {
    }

    /**
     * Creates a map named {@code termMap} to be return with the terms updated
     * and definitions if the keyword exists.
     *
     * @param fileName
     *            file name provided by user to get the input file to generate
     *            the map
     * @param terms
     *            Queue of strings, which contains the queue of terms
     * @return termMap Map generated by the method, which includes both terms
     *         and definitions
     * @requires filename to be not null or empty string
     * @requires terms to be not null
     * @requires termMap to be not null
     * @updates {@code terms}
     */
    public static Map<String, String> getTermMap(String fileName,
            Queue<String> terms) {
        assert fileName != null : "Violation of: fileName is not null";
        assert terms != null : "Violation of: terms is not null";

        // Reads the input file by user
        SimpleReader in = new SimpleReader1L(fileName);
        // Create a map to store the keys(terms) and values(definitions)
        Map<String, String> termMap = new Map1L<>();

        // Loop until the methods read the last line of the string
        while (!in.atEOS()) {
            // Store term in the string
            String term = in.nextLine();
            StringBuilder definition = new StringBuilder();
            // Store definitions in the string builder
            definition.append(in.nextLine());

            boolean hasNext = true;

            // Loop until next line of the definition is a blank
            while (hasNext && !in.atEOS()) {
                String line = in.nextLine();
                // If the next line is not blank, store another line to definition
                if (!line.isBlank()) {
                    definition.append(" " + line);
                } else {
                    // If is it a blank line, exit the loop
                    hasNext = false;
                }
            }

            // Convert string the builder to the string
            String def = definition.toString();
            // Update the queue term by adding each term
            terms.enqueue(term);
            // Store keys(terms) and values(definitions) in the map
            termMap.add(term, def);
        }

        // Call the comparator of string
        Comparator<String> cs = new StringLT();
        // Sort the terms in the queue by lexical order
        terms.sort(cs);

        in.close();

        return termMap;
    }

    /**
     * Creates the main HTML file named index.html, which includes the unordered
     * list of terms.
     *
     * @param fileName
     *            file name provided by user, which is a location where stores
     *            the index.html
     * @param terms
     *            queue of strings, which contains the queue of terms
     * @requires {@code terms} is not empty
     * @requires {@code fileName} is not empty
     * @ensures {@code terms} is not empty
     */
    public static void termPage(String fileName, Queue<String> terms) {
        assert fileName
                .length() != 0 : "Violation of: fileName must not be empty";
        assert terms.length() != 0 : "Violation of: terms must not be empty";

        // Store the inext.html in the folder name given by the user
        SimpleWriter out = new SimpleWriter1L("./" + fileName + "/index.html");
        // Assign a queue to restore the queue terms
        Queue<String> tCopy = new Queue1L<>();

        // Create header
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Sample Glossary</title>");
        out.println("</head>");

        // Open body
        out.println("<body>");
        out.println("<h2>Sample Glossary</h2>");

        // Insert a line
        out.println("<hr>");
        out.println("<h3>Index</h3>");

        // Create a unlisted order table based on the terms in the queue
        out.println("<ul>");
        while (terms.length() > 0) {
            String term = terms.dequeue();
            out.println("<li>");
            out.println("<a href=\"" + term + ".html\">" + term + "</a>");
            out.println("</li>");
            tCopy.enqueue(term);
        }
        // Restores the queue terms with copied queue tCopy
        terms.transferFrom(tCopy);

        // Close table, body, html
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");

        out.close();
    }

    /**
     * Separate the definition string by blank spaces( ), commas(,) and
     * periods(.). Return the updated string in the array to analyze only the
     * words in the definition
     *
     * @param def
     *            a string of definition
     * @return textArr an array of the only words in the definition
     * @requires {@code def} is not empty
     */
    public static String[] onlyWord(String def) {
        assert def != null : "Violation of: def is not null";

        // Assign a queue to store the only words
        Queue<String> simpleStr = new Queue1L<>();
        // Assign a string builder to store the only word
        StringBuilder str = new StringBuilder();

        int strNum = 0;

        // Loop until all the characters are compared
        for (int i = 0; i < def.length(); i++) {

            // Check rather the character does not contain period or comma
            if (!(def.charAt(i) == '.') && !(def.charAt(i) == ',')) {
                // If the character includes the space, add it to the string builder
                if (def.charAt(i) == ' ') {
                    strNum++;
                    String temp = str.toString();
                    simpleStr.enqueue(temp);
                    // Assign new string builder to keep generate the only word
                    str = new StringBuilder();
                } else {
                    // If the character is not a blank, keep adding each character to
                    // the string builder
                    str.append(def.charAt(i));
                    // If it reaches to the end, add the last word from the string builder
                    if (i + 1 == def.length()) {
                        strNum++;
                        String temp = str.toString();
                        simpleStr.enqueue(temp);
                    }
                }
            } else {
                // If the i reaches the end of the definition with ending period,
                // print the last end of the word.
                if (i + 1 == def.length() && def.charAt(i) == '.') {
                    strNum++;
                    String temp = str.toString();
                    simpleStr.enqueue(temp);
                }
            }
        }

        // Store the words in the queue in the array
        String[] textArr = new String[strNum];
        for (int i = 0; i < strNum; i++) {
            textArr[i] = simpleStr.dequeue();
        }

        return textArr;
    }

    /**
     * Creates the definition sub pages, which includes the definition with
     * related term's link and link to go back to the main page index.
     *
     * @param termMap
     *            map including all the terms and definitions
     * @param terms
     *            queue including all the terms
     * @param fileName
     *            file name provided by user, which is a location where stores
     *            the term.html
     * @requires {@code termMap} is not empty
     * @requires {@code terms} is not empty
     * @requires {@code fileName} is not empty
     */
    public static void definitionPage(Map<String, String> termMap,
            Queue<String> terms, String fileName) {
        assert termMap.size() != 0 : "Violation of: termMap must not empty";
        assert terms.length() != 0 : "Violation of: terms must not empty";
        assert fileName.length() != 0 : "Violation of: fileName must not empty";

        // Get the first term from the queue terms
        String term = terms.dequeue();
        // Output the file name to store the term.html in that location
        SimpleWriter out = new SimpleWriter1L(
                "./" + fileName + "/" + term + ".html");
        // Get definition string of the term
        String def = termMap.value(term);
        // Convert the definition string by getting only
        // words(excluding spaces, commas, and periods) and store them
        // as an array
        String[] arrDef = onlyWord(def);

        // Loop each word of the definition to compare the rest of the terms in the map
        for (String s : arrDef) {
            // If any word matches with the other term, replace it with the linked
            // term
            if (termMap.hasKey(s)) {
                def = def.replace(s,
                        "<a href = \"" + s + ".html\">" + s + "</a>");
            }
        }

        // Generate header
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + term + "</title>");
        out.println("</head>");

        // Open body
        out.println("<body>");
        out.println("<h2>");
        out.println("<b>");
        out.println("<i>");

        // Term with red color
        out.println("<font color = \"red\">" + term + "</font>");

        out.println("</i>");
        out.println("</b>");
        out.println("</h2>");

        // Print the definition
        out.println("<blockquote>");
        out.println(def);
        out.println("</blockquote>");

        // Insert a line
        out.println("<hr>");

        // Index link to go back to the main page
        out.println("<p>" + "Return to ");
        out.println("<a href = \"index.html\">index</a>");
        out.println(".");
        out.println("</p>");

        // Close body and html
        out.println("</body>");
        out.println("</html>");

        out.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        Queue<String> terms = new Queue1L<>();

        // Ask the user to input the name of file to use
        out.print("Enter the name of the file to load: ");
        String fileNameIn = in.nextLine();

        // Generate the map with keys(terms) and values(definitions)
        Map<String, String> termMap = getTermMap(fileNameIn, terms);

        // Ask the user to output the name of file to use
        out.print("Enter the location of the file to save: ");
        String fileName = in.nextLine();

        // Generate the index.html based on the terms in the file given by user
        termPage(fileName, terms);

        // Generate the term.html based on the terms and definitions in the
        // file given by user
        while (terms.length() > 0) {
            definitionPage(termMap, terms, fileName);
        }

        in.close();
        out.close();
    }

}
