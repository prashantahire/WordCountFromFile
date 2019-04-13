

package word.counter;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;


public class ReadInput {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String... args) {
        System.out.println("Please enter number of files to process and paths : ");
        Integer noOfFile = Integer.parseInt(scanner.nextLine().trim());
        List<String> fileNames = new ArrayList<String>();
         for(int i=0; i<noOfFile; i++) {
             fileNames.add(scanner.nextLine().trim());
         }
        Map<String, AtomicLong> result = new WordCountUtil().process(fileNames);
        System.out.println("Final word count result : ");
        System.out.println(result);
    }
}
