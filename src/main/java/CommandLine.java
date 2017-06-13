import com.optimizely.ab.Optimizely;
import com.optimizely.ab.config.Variation;
import com.optimizely.ab.config.parser.ConfigParseException;
import com.optimizely.ab.event.AsyncEventHandler;
import com.optimizely.ab.event.EventHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.net.URL;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.util.Scanner;
import java.util.stream.Stream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.nio.charset.StandardCharsets;

import java.util.Random;

/**
 * Reference implementation class intended to show a simple implementation of the Optimizely Full Stack Java SDK.
 * This application is command line based and simply asks over command line a name of a user.  That user will then be
 * placed in a variation by the SDK based on the data file provided.
 * Note: This implementation is currently hard coded to experiment 8429140525 in project 8410977336 provided
 * in the data file below and in https://cdn.optimizely.com/json/8410977336.json.
 * To use a different data file be sure to update the variation names used in: getVariationResponse.
 */
public class CommandLine {
    private String dataFile = ""; //instance variable to hold the datafile
    private EventHandler eventHandler; //Optimizely object responsible for dispatching events back to Optimizely
    private Optimizely optimizelyClient; //Optimizely client for determining variations and tracking events
    private Random randomNumbers; //Used to randomly select user events

    /**
     * Public constructor that will create the necessary
     * Optimizely components based on the provided data file url.
     * Local or remote file determined by presence of "http"
     * @param dataFile
     */
    public CommandLine(String dataFile){
        //Load the full data file either from a local file or from
        //Optimizely ex: https://cdn.optimizely.com/json/8410977336.json
        if(dataFile.startsWith("http")) {
            this.dataFile = CommandLine.loadRemoteDataFile(dataFile);
        } else {
            this.dataFile = CommandLine.loadLocalDataFile(dataFile);
        }
        this.eventHandler = new AsyncEventHandler(20000, 1);
        this.optimizelyClient = createOptimizelyClient(this.dataFile);
        this.randomNumbers = new Random(System.currentTimeMillis());
    }

    /**
     * Used to create an optimizely client.
     * This object is needed for user routing to variations and for
     * metric tracking.
     * @param dataFile
     * @return
     */
    private Optimizely createOptimizelyClient(String dataFile){
        Optimizely optimizelyClient = null;

        try {
            // Initialize an Optimizely client
            optimizelyClient = Optimizely.builder(dataFile, eventHandler).build();
        } catch (ConfigParseException e) {
            e.printStackTrace();
        }

        return optimizelyClient;
    }

    /**
     * Method for loading the Optimizely data file from Optimizely.
     * ex: https://cdn.optimizely.com/json/8410977336.json
     * @param urlString
     * URL of datafile
     * @return
     * The Optimizely data file as a string.
     */
    private static String loadRemoteDataFile(String urlString){
        URL url = null;
        InputStream in = null;

        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(urlString);
            in = url.openStream();
            InputStreamReader inputStream = new InputStreamReader( in );
            BufferedReader buf = new BufferedReader( inputStream );
            String line;
            while ( ( line = buf.readLine() ) != null ) {
                sb.append( line );
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if(in != null) {
                    in.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Method for loading the Optimizely data file from Optimizely.
     * ex: /Users/<User>/workspace/Optimizely/capital_one.json
     * @param urlString
     * local path of datafile
     * @return
     * The Optimizely data file as a string.
     */
    private static String loadLocalDataFile(String pathString){
        StringBuilder sb = new StringBuilder();
        Path path = Paths.get(pathString);

        //read file into stream, try-with-resources
        StringBuilder data = new StringBuilder();
        try {
            Stream<String> lines = Files.lines(path);
            lines.forEach(line -> sb.append(line));
            lines.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Method to get the variation that a given user is in.
     * @param userId
     * User to determine a variation for.
     * @return
     * Response based on the variation the user was put in.
     */
    public String getVariationResponse(String userId){
        String variationText = "Not Evaluated";

        Variation variation = this.optimizelyClient.activate("capital_one", userId);

        //Conditionals to determine which variation the user has been set to
        //NOTE: the variations are hard coded to an existing experiment.
        if (variation != null) {
            if (variation.is("treatment1")) {
                variationText = userId + " is in user variation 1";
            } else if (variation.is("treatment2")) {
                variationText = userId + " is in user variation 2";
            }
        } else {
            variationText = "You are not in the experiment audience";
        }
        return variationText;
    }

    /**
     * Method to arbitrarily determine if the user had a tracked event.
     * Using a 50/50 split for having an event
     * @param userId
     * User to determine event occurence for.
     * @return
     * Response based on the whether or not the the user had the event.
     */
    public String trackUser(String userId){
        String eventText = "No Event";

        int randomInt = this.randomNumbers.nextInt() % 2;

        if (randomInt == 0) {
            this.optimizelyClient.track("success_metric", userId);
            eventText = userId + " had an event!";
        } else if (randomInt == 1) {
            eventText = userId + " did not have an event";
        }

        return eventText;
    }

    public static void main(String[] args){
        // create a scanner so we can read the command-line input
        Scanner scanner = new Scanner(System.in);
        String response = "";

        String url = "";
        if(args.length > 0){
            url = args[0];
        }

        CommandLine clExperiment = new CommandLine(url);

        System.out.println("Enter quit to quit");

        while(response != null && !response.equals("quit")) {
            //  prompt for the user's name
            System.out.println("Enter a user id: ");

            // get their input as a String
            response = scanner.next();
            String variation = clExperiment.getVariationResponse(response);

            System.out.println(variation);

            String event = clExperiment.trackUser(response);
            System.out.println(event);
            System.out.println("");
        }
    }

//    Sample data file for reference:
//    {
//        "version": "2",
//            "projectId": "8410977336",
//            "experiments": [
//        {
//            "status": "Running",
//                "audienceIds": [],
//            "variations": [
//            {
//                "id": "8426432849",
//                    "key": "treatment1"
//            },
//            {
//                "id": "8423302614",
//                    "key": "treatment2"
//            }
//      ],
//            "id": "8429140525",
//                "key": "capital_one",
//                "layerId": "8420906043",
//                "trafficAllocation": [
//            {
//                "entityId": "8426432849",
//                    "endOfRange": 5000
//            },
//            {
//                "entityId": "8423302614",
//                    "endOfRange": 10000
//            }
//      ],
//            "forcedVariations": {}
//        }
//  ],
//        "audiences": [],
//        "groups": [],
//        "attributes": [],
//        "revision": "4",
//            "events": [
//        {
//            "experimentIds": [
//            "8429140525"
//      ],
//            "id": "8428250113",
//                "key": "success_event"
//        }
//  ],
//        "accountId": "8367000120"
//    }


}
