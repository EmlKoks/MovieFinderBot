
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;


@SuppressWarnings("deprecation")
public class OMDBBot {
	private static String url = "http://www.omdbapi.com/?plot=short&r=xml&i=tt";
	public static int startID=300000;
	private static int numThreads = 0;
	private static int threadsLimit=2000;
	private static int maxThreads=30;
	public static int numGet = 50;
	public static int count=0;
	private static Runnable[] runners;
    private static Thread[] threads;
    private static Date start;
    private static Date end;
	
	public static void main(String[] args) throws IOException{
		start= new Date();
		runners = new Runnable[maxThreads];
	    threads = new Thread[maxThreads];
        for(int i=0; i<maxThreads; ++numThreads, ++i) {
        	try{
	            runners[i]=new BotThread(numThreads*numGet, i);
	            threads[i]=new Thread(runners[i]);
	            threads[i].start();
	            System.out.println("Uruchomiono watek nr: "+ numThreads);
        	}catch(Exception e){
        		e.printStackTrace();
        		--numThreads;
        		break;
        	}
        }
	}
	
	public static synchronized void increaseCount(int a){
		++count;
		System.out.println("Skonczy³ siê watek: "+ count);
		if(numThreads<threadsLimit)        	
			try{
	            runners[a]=new BotThread(numThreads*numGet, a);
	            threads[a]=new Thread(runners[a]);
	            threads[a].start();
	            ++numThreads;
	            System.out.println("Uruchomiono watek nr: "+ numThreads);
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		--numThreads;
	    	}
		else if(count==numThreads)
			try{
				saveAllInOne();
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	private static synchronized void saveAllInOne() throws IOException{
		File sfile = new File("insert.sql");
		if (!sfile.exists()) {
			sfile.createNewFile();
		}	
		FileWriter fw = new FileWriter(sfile.getAbsoluteFile(), true);
		for(int i=0 ; i<numThreads ; ++i){
			File ins = new File("insert"+i*numGet+".sql");
			Scanner sc = new Scanner(ins);
	        while(sc.hasNextLine()) {
	            String s = sc.nextLine();
	            fw.write(s+"\n");
	        }
	        sc.close();		
	        ins.delete();
		}
		fw.close();
		end=new Date();
		System.out.println("Czas wykonywania: " + (end.getTime()-start.getTime()) + "ms");
		System.out.println("Liczba wyników: " + numGet*numThreads);
	}
	
	@SuppressWarnings("resource")
	public static String addMovie(double x) throws IOException{	
		DefaultHttpClient client;
		String ur=url +"0000000".substring(1+(int)(Math.log10(x)))+(int)x;
		//System.out.println(ur);
		HttpGet get = new HttpGet(ur);
		client = new DefaultHttpClient();
		HttpResponse response = client.execute(get);
		client.getConnectionManager().shutdown();
        String xmlString = EntityUtils.toString(response.getEntity());
        //System.out.println(xmlString);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
        	DocumentBuilder builder = factory.newDocumentBuilder();
        	Document document = builder.parse( new InputSource( new StringReader( xmlString ) ) ); 
        	NamedNodeMap el = document.getElementsByTagName("movie").item(0).getAttributes();    
        	return generateQuery(el);
        }catch(Exception e){
        	e.printStackTrace();
        	return "";
        }	
	}
	
	private static String generateQuery(NamedNodeMap el){
		StringBuilder insert = new StringBuilder();
		insert.append("INSERT INTO MOVIE VALUES(\'")
		.append(el.getNamedItem("title").getNodeValue())
		.append("\', \'")
		.append(el.getNamedItem("year").getNodeValue())
		.append("\', \'")
		.append(el.getNamedItem("director").getNodeValue())
		.append("\', NULL , \'") //trailer link
		.append(el.getNamedItem("poster").getNodeValue())
		.append("\', "+ new Random().nextFloat()*100+"  );");	
		//System.out.println(insert.toString());
		return insert.toString();
	}
}