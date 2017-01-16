package Bonus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deeplearning4j.examples.nlp.word2vec.Word2VecRawTextExample;

import SmoothingSearch.LMDirichlet;

public class Bonus {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		int chooseQuery = 7;
		String[] tenQueries = {"causes of stress", "weight loss", "aids in africa", "waterborne diseases in africa",
				"obesity in children",  "diabetes", "hair loss or baldness", "english as a second language",
				"playing guitar", "playstation 4"};
		String[] queryIds = {"001","002","003","004","005","006","007","008","009","010"};
		String rawSentences= "C:\\Users\\Ivo\\Desktop\\test\\raw_sentences_bonus.txt";
		String index = "C:\\workspace_java1_8\\TIR_Index_Dirichlet";
		
		Collection<Double> pAt5LanguageModel=new ArrayList<Double>();
		Collection<Double> pAt10LanguageModel=new ArrayList<Double>();

		Collection<Double> pAt5ExtendedModel=new ArrayList<Double>();
		Collection<Double> pAt10ExtendedModel=new ArrayList<Double>();
		for(int chooseQuery = 0; chooseQuery < 10; chooseQuery++){
			
			String q = tenQueries[chooseQuery];
			Collection<String> groundTruth = CSVReader.read(queryIds[chooseQuery]);
			
			try {
				Iterator<String> iter = groundTruth.iterator();
			      for(int i = 0; i< 10; i++){
			    	  System.out.println(iter.next());
			      }
				Collection<String> languageModelResults = LMDirichlet.search(index,q, 1000, 1000f);
				System.out.println("p@5 with languageModelResults: "
						+ Word2VecRawTextExample.precisionAtK(5,groundTruth, languageModelResults));
				System.out.println("p@10 with languageModelResults: "
						+ Word2VecRawTextExample.precisionAtK(10,groundTruth, languageModelResults));
				pAt5LanguageModel.add(Word2VecRawTextExample.precisionAtK(5,groundTruth, languageModelResults));
				pAt10LanguageModel.add(Word2VecRawTextExample.precisionAtK(10,groundTruth, languageModelResults));
				
				Collection<String> terms = extractTerms(LMDirichlet.getParsedQuery());
				System.out.println("without stop words" + terms);
				Collection<String> nearestWords = Word2VecRawTextExample.computeModel(100,5,true, rawSentences, terms);
				
				String expandedQuery = q;
				for(String s : nearestWords){
					expandedQuery += " " + s;
				}
				System.out.println("Expanded Query: "+expandedQuery);
				Collection<String> extendedResults = LMDirichlet.search(index,expandedQuery, 10, 1000f);
				System.out.println("p@5 with extendedResults: "
						+ Word2VecRawTextExample.precisionAtK(5,groundTruth, extendedResults));
				System.out.println("p@10 with extendedResults: "
						+ Word2VecRawTextExample.precisionAtK(10,groundTruth, extendedResults));
				pAt5ExtendedModel.add(Word2VecRawTextExample.precisionAtK(5,groundTruth, extendedResults));
				pAt10ExtendedModel.add(Word2VecRawTextExample.precisionAtK(10,groundTruth, extendedResults));
				
				terms = extractTerms(LMDirichlet.getParsedQuery());
				System.out.println("without stop words" + terms);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("p@5 using LanguageModel: " + pAt5LanguageModel);
		System.out.println("p@5 using ExtendedModel: " + pAt5ExtendedModel);
		System.out.println("p@10 using LanguageModel: " + pAt10LanguageModel);
		System.out.println("p@10 using ExtendedModel: " + pAt10ExtendedModel);
		System.out.println("p@5 using LanguageModel: " + mean(pAt5LanguageModel));
		System.out.println("p@10 using LanguageModel: " + mean(pAt10LanguageModel));
		System.out.println("p@5 using ExtendedModel: " + mean(pAt5ExtendedModel));
		System.out.println("p@10 using ExtendedModel: " + mean(pAt10ExtendedModel));
	}

	private static double mean(Collection<Double> pAt5LanguageModel) {
		// TODO Auto-generated method stub
		double result = 0.0;
		int count = 0;
		for(double k : pAt5LanguageModel){
			result += k;
			count++;
		}
		return result/count;
	}

	private static Collection<String> extractTerms(String parsedQuery) {
		// TODO Auto-generated method stub
		Collection<String> result = new ArrayList<String>();
		Pattern pattern = Pattern.compile("(?<=contents:)\\w+");
		Matcher matcher = pattern.matcher(parsedQuery);
		while(matcher.find()){
			result.add(matcher.group());
		}
		return result;
	}

}
