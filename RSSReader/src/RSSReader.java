import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Yoojin Jeong
 *
 */
public final class RSSReader {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSReader() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        //initialize title and description
        String title = "";
        String description = "";

        // Check if title is empty
        if (channel.child(getChildElement(channel, "title"))
                .numberOfChildren() > 0) {
            title = channel.child(getChildElement(channel, "title")).child(0)
                    .label();
        } else {
            title = "Empty Title";
        }

        // Check if description is empty
        if (channel.child(getChildElement(channel, "description"))
                .numberOfChildren() > 0) {
            description = channel.child(getChildElement(channel, "description"))
                    .child(0).label();
        }

        //print out tags
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>");
        out.println(
                "<a href=\""
                        + channel.child(getChildElement(channel, "link"))
                                .child(0).label()
                        + "\">" + title + "</a></h1>");
        out.println("<p>" + description + "</p>");
        //print out border and titles of the table
        out.println("<table border=\"1\">");
        out.println("<tr>");
        out.println("<th>Date</th>");
        out.println("<th>Source</th>");
        out.println("<th>News</th>");
        out.println("</tr>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        //close table, body, html tags
        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        int index = -1;
        int i = 0;

        //while loop to find first occurrence of the given tag
        while (i < xml.numberOfChildren() && index == -1) {
            if (xml.child(i).label().equals(tag)) {
                index = i;
            }
            i++;
        }
        //return index
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        //initialize variables
        int i = 0;
        String date = "";
        String source = "";
        String title = "";
        String description = "";

        //for each children of item,
        //check if there is pubdate, source, title or description
        while (i < item.numberOfChildren()) {
            //check if pubDate exists
            if (item.child(i).label().equals("pubDate")) {
                date = item.child(i).child(0).label();
            }

            //check if source exists
            if (item.child(i).label().equals("source")) {
                //check if url exists
                if (item.child(i).hasAttribute("url")) {
                    source = "<a href = \""
                            + item.child(i).attributeValue("url") + "\">"
                            + item.child(i).child(0).label() + "</a>";
                } else {
                    source = item.child(i).child(0).label();
                }
            }

            //index of tag named "link"
            int indexLink = getChildElement(item, "link");
            //check if title exists
            if (item.child(i).label().equals("title")) {
                //if there is a link, put hyperlink to the title
                if (indexLink != -1) {
                    title = ("<a href=\""
                            + item.child(indexLink).child(0).label() + "\">"
                            + item.child(i).child(0).label() + "</a>");
                } else {
                    title = item.child(i).child(0).label();
                }
            }

            //check if description exists
            if (item.child(i).label().equals("description")) {
                description = item.child(i).label();
                //if there is a link, put hyperlink to the description
                if (indexLink != -1) {
                    description = ("<a href=\""
                            + item.child(indexLink).child(0).label() + "\">"
                            + item.child(i).child(0).label() + "</a>");
                } else {
                    description = item.child(i).child(0).label();
                }
            }

            //all the tags are optional
            if (date.equals("")) {
                date = "No date available";
            }
            if (source.equals("")) {
                source = "No source available";
            }
            if (title.equals("")) {
                title = description;
            }

            i++;
        }
        //print out the table row
        out.println("<tr>");
        out.println("<td>" + date + "</td>");
        out.println("<td>" + source + "</td>");
        out.println("<td>" + title + "</td>");
        out.println("</tr>");
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

        //ask the user for URL and read the RSS feed into an XMLTree object
        out.print("Enter the URL of an RSS 2.0 feed: ");
        String url = in.nextLine();
        XMLTree rss = new XMLTree1(url);

        //check that the label of the root of the XMLTree is an <rss> tag
        //and check that it has a version attribute with value "2.0"
        while (rss.label() == "rss" && rss.attributeValue("version") == "2.0") {
            out.println("Invalid RSS feed.");

            out.print("Enter the URL of an RSS 2.0 feed: ");
            url = in.nextLine();
            rss = new XMLTree1(url);
        }

        //ask user the name of the file
        out.print("Enter the file name you will create: ");
        String fileName = in.nextLine();

        //create output file as file name.
        SimpleWriter outputFile = new SimpleWriter1L(fileName);

        //source is subtree starting from channel tag
        XMLTree source = new XMLTree1(url).child(0);
        outputHeader(source, outputFile);

        //call processItem to create table structured content.
        int count = 0;
        while (count < source.numberOfChildren()) {
            if (source.child(count).label().equals("item")) {
                processItem(source.child(count), outputFile);
            }
            count++;
        }

        outputFooter(outputFile);

        in.close();
        out.close();
        outputFile.close();
    }

}
