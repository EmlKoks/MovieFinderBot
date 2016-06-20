import java.io.File;
import java.io.FileWriter;

public class BotThread implements Runnable {
	private int id;
	private int idInList;
	private File sfile;
		
	public BotThread(int id, int idInList){
		this.id=id;
		this.idInList=idInList;
	}

	@Override
	public void run() {
		FileWriter fw=null;
		try{
			sfile = new File("insert"+id+".sql");//tu zapisuje linki
			if (!sfile.exists()) {
				sfile.createNewFile();
			}
			fw = new FileWriter(sfile.getAbsoluteFile());
			for(int i=id ; i<id+OMDBBot.numGet ; ++i) fw.write(OMDBBot.addMovie(i+1+OMDBBot.startID)+"\n");
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
			try{
				fw.close();
			}catch(Exception x){}
		}finally{
			OMDBBot.increaseCount(idInList);
		}
	}

}
