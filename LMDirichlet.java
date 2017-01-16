package SmoothingSearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class LMDirichlet {

	private static String parsedQuery = "";
	
	public static String getParsedQuery() {
		return parsedQuery;
	}

	public static Collection<String> search(String indexDir,String q, int topK, float mu) throws IOException, ParseException {
		Path path = Paths.get(indexDir);
		IndexReader rdr = DirectoryReader.open(FSDirectory.open(path));
		LMSimilarity sim= new LMSimilarity() {
			
			@Override
			protected float score(BasicStats stats, float freq, float doclen) {
//				System.out.println(stats.getDocFreq()+" term freq "+stats.getTotalTermFreq()+ " f: "+freq);
				return (freq +	mu* collectionModel.computeProbability(stats))
						/(doclen+mu)
						;
			}
			
			@Override
			public String getName() {
				return "custom dirichlet";
			}
		};
		
		IndexSearcher is = new IndexSearcher(rdr);
//		Similarity sim = new LMDirichletSimilarity(mu);
		is.setSimilarity(sim);
		QueryParser parser =new QueryParser("contents",	new StandardAnalyzer());
		
		TopDocs hits;
		if(q.matches(".+@\\d{4}-\\d{4}")){
			String[] split = q.split("@");
			String keyword = split[0];
			String[] years = split[1].split("-");
			String start = years[0];
			String end = years[1];
	
			Query query = parser.parse(keyword);
			
			QueryParser dateparser = new QueryParser("date", new StandardAnalyzer());
			Query datequery = dateparser.parse("["+start+" TO "+end+"]");
			
			Builder bq= new BooleanQuery.Builder();
			bq.add(query, BooleanClause.Occur.MUST);
			bq.add(datequery, BooleanClause.Occur.MUST);
			
			hits = is.search(bq.build(), topK);
//			System.out.println(is.count(bq.build()));
			
		}else{
			Query query = parser.parse(q);
			parsedQuery = query.toString();
			System.out.println(query);
			hits = is.search(query,topK);
		}
		File fixedF =new File("C:\\Users\\Ivo\\Desktop\\test\\raw_sentences_bonus.txt");
//		Files.delete(Paths.get(URI.create("file:///C:/Users/Ivo/Desktop/test/raw_sentences_bonus.txt")));
//		System.out.println(fixedF.delete());
		FileOutputStream fout2 = new FileOutputStream(fixedF);
		fout2.write(" ".getBytes("UTF-8"));
		fout2.close();
		
		Collection<String> result = new ArrayList<String>();
		for(ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			if(topK==1000){		
				FileOutputStream fout = new FileOutputStream(fixedF,true);
				fout.write(doc.get("contents").getBytes("UTF-8"));
				fout.close();
			}else{
				System.out.println(doc.get("id"));
			}
			result.add(doc.get("id"));
		}
		return result;
	}
   
	public static void main(String[] args) {
		int topK = 0;
		String query = "";
		float parameter = 0.0f;

		for(int i = 0; i< args.length-2; i++){
			query = query.concat(args[i]);
			if(i<args.length-3)
				query = query.concat(" ");
		}
//		System.out.println(query);
		topK = Integer.parseInt(args[args.length-2]);
		parameter = Float.parseFloat(args[args.length-1]);

		
		String indexdir = "C:\\workspace_java1_8\\TIR_Index_Dirichlet";
		
		try{
			LMDirichlet.search(indexdir,"donald trump@2011-2013",5,100f);
			System.out.println("\n");
			LMDirichlet.search(indexdir,"donald trump@2011-2013",5,1000f);
			System.out.println("\n");
			LMDirichlet.search(indexdir,"donald trump@2011-2013",5,2000f);
//			LMDirichlet.search(indexdir, query, topK, parameter);
		} catch (Exception e){
			e.printStackTrace();
			
		}
	}
//	sync3-20120203040101_756
//	sync3-20110720040101_3337
//	sync3-20120405040101_951
//	lk-20120110040101_1415
//	lk-20121108040101_4940
//
//
//	sync3-20120405040101_951
//	sync3-20120203040101_756
//	lk-20120721040101_1485
//	lk-20120721040101_2591
//	lk-20110610040101_4463
//
//
//	sync3-20120405040101_951
//	lk-20120721040101_1485
//	lk-20120721040101_2591
//	sync3-20120203040101_756
//	lk-20110610040101_4463
}
