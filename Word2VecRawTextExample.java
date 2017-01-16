package org.deeplearning4j.examples.nlp.word2vec;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by agibsonccc on 10/9/14.
 *
 * Neural net that processes text into wordvectors. See below url for an in-depth explanation.
 * https://deeplearning4j.org/word2vec.html
 */
public class Word2VecRawTextExample {

    private static Logger log = LoggerFactory.getLogger(Word2VecRawTextExample.class);

    public static void main(String[] args) throws Exception {
    	int dimension = 0;
		int windowsize = 0;
		boolean useSkipgram = true;
//		System.out.println(query);
		dimension = Integer.parseInt(args[0]);
		windowsize= Integer.parseInt(args[1]);
		useSkipgram = Boolean.parseBoolean(args[2]);
		
//		computeModel(100, windowsize, useSkipgram, "C:\\Users\\Ivo\\Desktop\\test\\raw_sentences_in_lines.txt");
		computeMetrics("diabetes", "C:\\Users\\Ivo\\Desktop\\test\\pathToSaveModel_300.txt", "C:\\Users\\Ivo\\Desktop\\test\\pathToSaveModel_500.txt");
//		computeMetrics("earthquake", "C:\\Users\\Ivo\\Desktop\\test\\pathToSaveModel_100.txt", "C:\\Users\\Ivo\\Desktop\\test\\pathToSaveModel_500.txt");
		
    }

	private static void computeMetrics(String word, String embedding, String groundTruth) throws FileNotFoundException {
		//load model ground truth
      Word2Vec word2Vec = WordVectorSerializer.loadFullModel(groundTruth);
      //load model embedding
    Word2Vec word2VecEmbedding = WordVectorSerializer.loadFullModel(embedding);
       
      // Prints out the closest 10 words to "word". An example on what to do with these Word Vectors.
      log.info("Closest Words:");
      Collection<String> lst = word2Vec.wordsNearest(word, 10);
      System.out.println("10 Words closest to" + word + ": " + lst);
         
   // Prints out the closest 10 words to "word". An example on what to do with these Word Vectors.
      log.info("Closest Words:");
      Collection<String> lstembedding = word2VecEmbedding.wordsNearest(word, 10);
      System.out.println("10 Words closest to"+ word +": " + lstembedding);
      
      Collection<String> lst2 = word2Vec.wordsNearest("earthquake", 10);
      System.out.println("10 Words closest to earthquake : " + lst2);
         
      Collection<String> lstembedding2 = word2VecEmbedding.wordsNearest("earthquake", 10);
      System.out.println("10 Words closest to earthquake: " + lstembedding2);
      
      //precision @k
      double result = precisionAtK(5, lst, lstembedding);
      double result2 = precisionAtK(5, lst2, lstembedding2);
      System.out.println("p@5: " +result);
      System.out.println("p@5: " +result2);
      
      //recall
     double r = recall(lst, lstembedding);
     double r2 = recall(lst2, lstembedding2);
     System.out.println("recall: " + r);
     System.out.println("recall: " + r2);
      
      //MAP
      double r3 = map(lst, lstembedding);
      double r4 = map(lst2, lstembedding2);
      System.out.println("map: " +r3);
      System.out.println("map: " +r4);
      //NDCG
      double r5 = ndcg(lst, lstembedding);
      double r6 = ndcg(lst2, lstembedding2);
      System.out.println("ndcg: " + r5);
      System.out.println("ndcg: " + r6);
	}

	private static double ndcg(Collection<String> lst,
			Collection<String> lstembedding) {
		Iterator<String> iter2 = lstembedding.iterator();
	      double sum2 = 0;
	      double groundTruthDCG = 3/(log(2))+3/(log(3))+3/(log(4))+3/(log(5))+3/(log(6))
	    		  +1/(log(7))+1/(log(8))+1/(log(9))+1/(log(10))+1/(log(11));
	      for(int a = 0; a<lstembedding.size(); a++){
	    	  String w = iter2.next();
	    	  int b = relevance(w, lst);
	    	  if(lst.contains(w)) {
	    		  sum2 += ((Math.pow(2, b)-1)/log(a+2));
	       	  }
	      }
	      
		return sum2/groundTruthDCG;
	}

	private static double map(Collection<String> lst,
			Collection<String> lstembedding) {
		Iterator<String> iter = lstembedding.iterator();
	      double sum = 0;
	      int anzahl = 0;
	      for(int a = 0; a<lstembedding.size(); a++){
	    	  String w = iter.next();
	    	  if(lst.contains(w)) {
	    		  sum += precisionAtK(a+1, lst, lstembedding);
	    		  anzahl++;
	    	  }
	      }	      
		return sum/anzahl;
	}

	private static double recall(Collection<String> lst,
			Collection<String> lstembedding) {
		double counter2 =0;
	      for (String s: lst){
	    	  if (lstembedding.contains(s)) counter2++;
	      }
		return counter2/10;
	}

	private static int relevance(String w, Collection<String> lst) {
		// TODO Auto-generated method stub
		 Iterator<String> iter2 = lst.iterator();
		 for(int i = 0; i< 5; i++){
			 if(iter2.next().equals(w))return 2;
		 }
		 for(int i = 5; i<10 ; i++){
			 if(iter2.next().equals(w))return 1;
		 }
		return 0;
	}

	private static double log(int i) {
		// TODO Auto-generated method stub
		return Math.log(i);
	}

	public static double precisionAtK(int k, Collection<String> lst,
			Collection<String> lstembedding) {
		int counter =0;
		Iterator<String> iter = lstembedding.iterator();
	      for(int i = 0; i< k; i++){
	    	  String w = iter.next();
	    	  if(lst.contains(w)) counter++;
	      }
		return (double)counter/k;
	}

	public static Collection<String> computeModel(int dimension, int windowsize, boolean useSkipgram, String filePath, Collection<String> terms) throws Exception{
		// Gets Path to Text file
//        String filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath();

        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
//        SentenceIterator iter = new FileSentenceIterator(new File(filePath)) ;
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
        Word2Vec vec;
        if(useSkipgram){
        		 vec= new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(dimension)
                .seed(42)
                .windowSize(windowsize)
                .iterate(iter)
                .tokenizerFactory(t)
                
//                .elementsLearningAlgorithm(new CBOW())
//                .elementsLearningAlgorithm(new SkipGram())
                .build();
        }else{//CBOW
        	vec = new Word2Vec.Builder()
            .minWordFrequency(5)
            .iterations(1)
            .layerSize(dimension)
            .seed(42)
            .windowSize(windowsize)
            .iterate(iter)
            .tokenizerFactory(t)
            .elementsLearningAlgorithm(new CBOW())
//            .elementsLearningAlgorithm(new SkipGram())
            .build();
        }
        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");

        // Write word vectors to file
//        WordVectorSerializer.writeWordVectors(vec, "C:\\Users\\Ivo\\Desktop\\test\\pathToWriteto.txt");

        //save model
        log.info("Save vectors....");
//        WordVectorSerializer.writeFullModel(vec, "C:\\Users\\Ivo\\Desktop\\test\\pathToSaveModel_bonus.txt");
        
        //load model
//        Word2Vec word2Vec = WordVectorSerializer.loadFullModel("C:\\Users\\Ivo\\Desktop\\test\\pathToSaveModel.txt");
        
        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        log.info("Closest Words:");
        Collection<String> lst = new ArrayList<String>();;
        for(String s : terms){
        	lst.addAll(vec.wordsNearest(s, 5));
        }
        System.out.println("Words closest to'"+terms+"': "+ lst);

        return lst;
        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());		
	}
}
