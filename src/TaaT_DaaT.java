import java.io.*;
import java.util.*;

public class TaaT_DaaT {
	static HashMap<String,PostingsList> daat;
	static HashMap<String,PostingsList> taat;
	static BufferedWriter bufferWriter;

	public TaaT_DaaT(){
		daat = new HashMap<>(); //HashMap for document-at-a-time
		taat = new HashMap<>(); //HashMap for term-at-a-time
	}
	
	public void Indexing(String indexFile, String logFile){
		String line = null;
		try{
			FileReader reader = new FileReader(indexFile);
			BufferedReader bufferReader = new BufferedReader(reader);
			
			while((line = bufferReader.readLine())!=null){
				PostingsList pl_daat = new PostingsList();
				PostingsList pl_taat = new PostingsList();
				
				int endIndexOfKey = line.indexOf("\\c");
				int endIndexOfPostingsSize = line.indexOf("\\m");
				
				/* term - used as a key to the HashMap */
				String term = line.substring(0,endIndexOfKey);		
				
				/* size of the postings list */
				String postingsSize = line.substring(endIndexOfKey+2,endIndexOfPostingsSize);	
				pl_daat.postingsSize = Integer.parseInt(postingsSize);
				pl_taat.postingsSize = Integer.parseInt(postingsSize);
				
				/* Actual postings */
				String postingsList = line.substring(endIndexOfPostingsSize+3,line.indexOf(']'));
				postingsList = postingsList.replaceAll(",", "");
				
				int beginIndex=0; 
				
				while(beginIndex<postingsList.length()){
					Postings postings = new Postings();
					int indexOfSlash = postingsList.indexOf('/', beginIndex);
					if(indexOfSlash==-1)
						break;
					String doc_id = postingsList.substring(beginIndex,indexOfSlash);
					postings.Doc_id = Integer.parseInt(doc_id);
					int indexOfSpace = postingsList.indexOf(' ', indexOfSlash+1);
					String doc_freq = "";
					if(indexOfSpace==-1){
						doc_freq = postingsList.substring(indexOfSlash+1);
						postings.freq = Integer.parseInt(doc_freq);
						
						/*insert the array in the document-at-a-time HashMap*/
						insertionByDocId(pl_daat.postingsList,postings);
						
						/*insert the array in the term-at-a-time HashMap*/
						insertionByTermFreq(pl_taat.postingsList,postings);
						
						break;
					}	
					else{
						doc_freq = postingsList.substring(indexOfSlash+1, indexOfSpace);
						beginIndex = indexOfSpace + 1;
						postings.freq = Integer.parseInt(doc_freq);

						/*insert the array in the document-at-a-time HashMap*/
						insertionByDocId(pl_daat.postingsList,postings);
						
						/*insert the array in the term-at-a-time HashMap*/
						insertionByTermFreq(pl_taat.postingsList,postings);
					}
				}
		
				daat.put(term, pl_daat);
				taat.put(term, pl_taat);
			}
				
			bufferReader.close();
		}
		catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + indexFile + "'");                
        }
		catch(IOException ex){
			System.out.println("Error reading file");
		}
	}
	
	/* function to insert the document id in the TAAT Linked Lists in the decreasing order of term frequency */
	private void insertionByTermFreq(LinkedList<Postings> postingsList, Postings valueToInsert) {
		int count_curr = 0;
		if(postingsList.isEmpty()){ //if the list is empty simply insert the value and return
			postingsList.add(valueToInsert);
			return;
		}
		for(Postings current : postingsList){ //otherwise look for the suitable place to insert the document id
			count_curr = postingsList.indexOf(current);
			if(valueToInsert.freq.intValue()>current.freq.intValue()){
				postingsList.add(count_curr,valueToInsert);
				break;
			}
			else if(valueToInsert.freq.intValue()==current.freq.intValue()){
				if(valueToInsert.Doc_id.intValue()<current.Doc_id.intValue()){
					postingsList.add(count_curr,valueToInsert);
					break;
				}
				else if(postingsList.getLast().equals(current)){
					postingsList.addLast(valueToInsert);
					break;
				}
			}
			else if(postingsList.getLast().equals(current)){
				postingsList.addLast(valueToInsert);
				break;
			}
		}	
	}

	/* function to insert the document id in the DAAT Linked Lists in the increasing order of document id */
	private void insertionByDocId(LinkedList<Postings> postingsList, Postings valueToInsert) {
		int count_curr = 0;
		if(postingsList.isEmpty()){ //if the list is empty simply insert the value and return
			postingsList.add(valueToInsert);
			return;
		}
		for(Postings current : postingsList){ //otherwise look for the suitable place to insert the document id
			if(valueToInsert.Doc_id.intValue()<current.Doc_id.intValue()){
				count_curr = postingsList.indexOf(current);
				postingsList.add(count_curr,valueToInsert);
				break;
			}
			else if(postingsList.getLast().equals(current)){//when we reached the end of the list, we need to insert at the last position
				postingsList.addLast(valueToInsert);
				break;
			}
		}
	}

	/* Returns the terms with K largest postings lists */
	private void getTopK(Integer K){
		LinkedList<TopK> topK = new LinkedList<>();
		int count_curr = 0;
		for(String key : daat.keySet()){
			TopK node = new TopK();
			node.postingSize = daat.get(key).postingsSize;
			node.term = key;
					
			if(topK.size()<K){//fill the list with K terms initially in sorted order
				if(topK.isEmpty()){
					topK.add(node);
					continue;
				}
				for(TopK current : topK){
					if(node.postingSize.intValue()>current.postingSize.intValue()){
						count_curr = topK.indexOf(current);
						topK.add(count_curr,node);
						break;
					}
					else if(topK.getLast().equals(current)){//when we reached the end of the list, we need to insert at the last position
						topK.addLast(node);
						break;
					}
				}
			}
			else{ //compare every posting size with the first and last element in the list. if it is in the range, insert else ignore
				if(node.postingSize<topK.peekLast().postingSize){
					continue;
				}
				else if(node.postingSize>=topK.peekFirst().postingSize){
					topK.removeLast();
					topK.addFirst(node);
				}
				else{//in between the two
					//remove the first element in the list, and insert the element in sorted position
					topK.removeLast();
					for(TopK current : topK){
						if(node.postingSize.intValue()>current.postingSize.intValue()){
							count_curr = topK.indexOf(current);
							topK.add(count_curr,node);
							break;
						}
					}	
				}
			}
		}
		try{
			bufferWriter.write("Result: ");
			for(TopK current : topK){
				if(current.equals(topK.getLast())){
					bufferWriter.write(current.term);
				}
				else{
					bufferWriter.write(current.term + ", ");
				}
			}
			bufferWriter.newLine();
		}
		catch(IOException e){
			System.out.println("Output File not found");
		}	
	}
	
	private LinkedList<String> optimizedTerms(LinkedList<String> terms){/* sort the terms based on the postings size */
		LinkedList<String> sortedTerms = new LinkedList<>();
		int count_curr = 0;
		for(String term : terms){
			String node = term;
			if(sortedTerms.isEmpty()){
				sortedTerms.add(node);
				continue;
			}
			for(String current : sortedTerms){
				if(daat.get(current).postingsSize.intValue()>daat.get(node).postingsSize.intValue()){
					count_curr = sortedTerms.indexOf(current);
					sortedTerms.add(count_curr,node);
					break;
				}
				else if(sortedTerms.getLast().equals(current)){//when we reached the end of the list, we need to insert at the last position
					sortedTerms.addLast(node);
					break;
				}
			}
		}	
		return sortedTerms;
	}
	
	/* Insert the linkedList of the first term in the intermediate List and compare every element of both the lists. If the elements match, add to 
	 the result list. Empty the contents of intermediate list and copy the result list. So as to compare the elements with the subsequent 
	 lists. Final list is sorted at the end to generate the result
	*/
	private void termAtATimeQueryAnd(LinkedList<String> terms, boolean optimized){
		long startTime = System.currentTimeMillis();
		int comparisonsCount = 0;
		LinkedList<Integer> intermediateList = new LinkedList<>();
		LinkedList<Integer> resultList = new LinkedList<>();
		
		if(optimized==true){
			terms = optimizedTerms(terms);
		}
				
		for(String term : terms){
			LinkedList<Integer> ll = new LinkedList<>();
			getPostingsByTermFq(term,ll);
			
			if(intermediateList.isEmpty()){
				intermediateList.addAll(ll);
				continue;
			}
			
			for(Integer n : ll){
				for(Integer x : intermediateList){
					comparisonsCount++;
					if(n.intValue()==x.intValue()){ // if the terms match push to the result list
						resultList.add(x);
						break;
					}
				}
			}
						
			intermediateList.clear();
			intermediateList.addAll(resultList); //copy the result list to intermediate list for comparisons with subsequent terms
			if(resultList.isEmpty()){
				break;
			}
			resultList.clear();
		}
		Collections.sort(intermediateList);

		long stopTime = System.currentTimeMillis();
		double elapsedTime = (double)(stopTime - startTime);
		elapsedTime = elapsedTime * 0.001;
		try{
			if(!optimized){
				bufferWriter.write(intermediateList.size() + " documents are found ");
				bufferWriter.newLine();
				bufferWriter.write(comparisonsCount + " comparisons are made");
				bufferWriter.newLine();
				bufferWriter.write(elapsedTime + " seconds are used");
				bufferWriter.newLine();
			}
			else{
				bufferWriter.write(comparisonsCount + " comparisons are made with optimization (optional bonus part)");
				bufferWriter.newLine();
				bufferWriter.write("Result: "); 
				for(Integer n : intermediateList){
					if(intermediateList.indexOf(n) == (intermediateList.size()-1))
						bufferWriter.write(n + "");
					else
						bufferWriter.write(n + ", ");
				}
				bufferWriter.newLine();
			}
		}catch(IOException e){
			System.out.println("Output File not found!");
		}
	}
	
	@Override
	public String toString() {
		return "TaaT_DaaT [daat=" + daat + ", taat=" + taat + "]";
	}

	/* take all the document ids of the lists one at a time and place them in sorted order. */
	private void termAtATimeQueryOr(LinkedList<String> terms, boolean optimized){
		long startTime = System.currentTimeMillis();
		LinkedList<Integer> resultList = new LinkedList<>();
		int comparisonsCount = 0;
		
		if(optimized==true){
			terms = optimizedTerms(terms);
		}
		
		for(String term : terms){
			LinkedList<Integer> ll = new LinkedList<>();
			getPostingsByTermFq(term,ll);
			
			while(!ll.isEmpty()){
				Integer docId = ll.pop();
				if(resultList.isEmpty()){
					resultList.add(docId);
					continue;
				}
				int count_curr = 0;
				if(docId.intValue()>=resultList.getLast().intValue()){
					comparisonsCount++;
					if(docId.intValue()>resultList.getLast().intValue())
						resultList.addLast(docId);
					continue;
				}
				for(Integer currID : resultList){//insertion in sorted order
					comparisonsCount++;
					if(docId.intValue()==currID.intValue()){
						break;
					}
					else if(docId.intValue()<currID.intValue()){
						count_curr = resultList.indexOf(currID);
						resultList.add(count_curr,docId);
						break;
					}
					else if(resultList.getLast().equals(currID)){
						resultList.addLast(docId);
						break;
					}
				}
			}
		}

		long stopTime = System.currentTimeMillis();
		double elapsedTime = (double)(stopTime - startTime);
		elapsedTime = elapsedTime * 0.001;
		try{
			if(!optimized){
				bufferWriter.write(resultList.size() + " documents are found ");
				bufferWriter.newLine();
				bufferWriter.write(comparisonsCount + " comparisons are made");
				bufferWriter.newLine();
				bufferWriter.write(elapsedTime + " seconds are used");
				bufferWriter.newLine();
			}
			else{
				bufferWriter.write(comparisonsCount + " comparisons are made with optimization (optional bonus part)");
				bufferWriter.newLine();
				bufferWriter.write("Result: "); 
				for(Integer n : resultList){
					if(n.intValue()==resultList.getLast())
						bufferWriter.write(n + "");
					else
						bufferWriter.write(n + ", ");
				}
				bufferWriter.newLine();
			}
		}catch(IOException e){
			System.out.println("Output File not found!");
		}
	}
	
	/*store all the LL in a hash map. Peek the first element and add to array. If max & min of array are equal
	then add to result list. else, pop all the elements of the list until their first element is greater than 
	max. compute max again and loop */
	private void docAtATimeQueryAnd(LinkedList<String> terms){
		long startTime = System.currentTimeMillis();
		LinkedList<Integer> resultList = new LinkedList<>();
		HashMap<String,LinkedList<Integer>> map = new HashMap<>();
		int comparisonsCount = 0;
		ArrayList<Integer> array = new ArrayList<>();
		for(String term : terms){
			LinkedList<Integer> ll = new LinkedList<>();
			getPostingsByDocId(term,ll);
			map.put(term,ll);
			array.add(ll.peek());
		}

		int size = array.size();
		while(size>0){
			if(Collections.max(array).equals(Collections.min(array))){//if max and min elements are equal that means all the elements are equal
				resultList.add(array.get(0));
				array.clear();
				for(String key : map.keySet()){
					LinkedList<Integer> tmp = map.get(key);
					tmp.pop();
					if(tmp.peek()!=null)
						array.add(tmp.peek());
					else{
						size = 0;
					}
				}
			}
			else{
				Integer max = Collections.max(array);
				array.clear();
				for(String key : map.keySet()){
					LinkedList<Integer> tmp = map.get(key);
					Iterator<Integer> iter = tmp.iterator();
					for(; iter.hasNext();){ //pop all the docIds which are less than max. Again store the remaining list into the map. This way it will be easier to track the elements not pushed to result set
						comparisonsCount++;
						if(iter.next().intValue() < max){
							if(tmp.size()>=1){
								tmp.pop();
								iter = tmp.iterator();
							}	
						}
						else{
							break;
						}
					}
					if(size>tmp.size())
						size = tmp.size();
					array.add(tmp.peek());
					map.put(key, tmp);
				}
			}
		}
		
		long stopTime = System.currentTimeMillis();
		double elapsedTime = (double)(stopTime - startTime);
		elapsedTime = elapsedTime * 0.001;
		try{
			bufferWriter.write(resultList.size() + " documents are found ");
			bufferWriter.newLine();
			bufferWriter.write(comparisonsCount + " comparisons are made");
			bufferWriter.newLine();
			bufferWriter.write(elapsedTime + " seconds are used");
			bufferWriter.newLine();
			bufferWriter.write("Result: "); 
			for(Integer n : resultList){
				if(n.intValue()==resultList.getLast())
					bufferWriter.write(n + "");
				else
					bufferWriter.write(n + ", ");
			}
			bufferWriter.newLine();
		}catch(IOException e){
			System.out.println("Output File not found!");
		}
	}
	
	/*store all the linkedlists in a hashmap. peek their first elements and find out the minimum
	 then in a loop compare the lists with the min element. if the first elem equals min then shift its pointer
	 and continue*/
	private void docAtATimeQueryOr(LinkedList<String> terms){
		long startTime = System.currentTimeMillis();
		LinkedList<Integer> resultList = new LinkedList<>();
		HashMap<String,LinkedList<Integer>> map = new HashMap<>();
		int comparisonsCount = 0;
		LinkedList<Integer> firstElemList = new LinkedList<>();
		LinkedList<Integer> sizeArray = new LinkedList<>();
		for(String term : terms){ 
			LinkedList<Integer> ll = new LinkedList<>();
			getPostingsByDocId(term,ll);
			map.put(term,ll);
			firstElemList.add(ll.peek()); //add all the first elements of all the lists to find the minimum, which will be added to the result list
			sizeArray.add(ll.size()); //the sizeArray keeps a track of number of elements yet to add to the result list. If size gets 0 we will remove the list from the hash map 
		}
		
		Integer min = 0;
		while(!sizeArray.isEmpty() && !Collections.max(sizeArray).equals(0)){//loop executes till all the lists are not empty
			min = Collections.min(firstElemList); //fetch the minimum element from the firstElement List and add it to the resultList
			resultList.addLast(min);
			firstElemList.clear(); //refresh the lists
			sizeArray.clear();
			for(String key : map.keySet()){ //remove min element<already added to result list> from all the lists and increment the pointer, this way duplicates will be taken care off
				LinkedList<Integer> tmp = map.get(key);
				if(tmp.size()==0){
					sizeArray.add(tmp.size());
					continue;
				}
				comparisonsCount++;	
				if(tmp.peek().equals(min)){
					if(tmp.size()>=1){
						tmp.pop();
					}
					map.put(key, tmp);
				}
				if(tmp.size()!=0)
					firstElemList.add(tmp.peek());
				sizeArray.add(tmp.size());
			}
		}
		long stopTime = System.currentTimeMillis();
		double elapsedTime = (double)(stopTime - startTime);
		elapsedTime = elapsedTime * 0.001;
		try{
			bufferWriter.write(resultList.size() + " documents are found ");
			bufferWriter.newLine();
			bufferWriter.write(comparisonsCount + " comparisons are made");
			bufferWriter.newLine();
			bufferWriter.write(elapsedTime + " seconds are used");
			bufferWriter.newLine();
			bufferWriter.write("Result: "); 
			for(Integer n : resultList){
				if(n.intValue()==resultList.getLast())
					bufferWriter.write(n + "");
				else
				bufferWriter.write(n + ", ");
			}
			bufferWriter.newLine();
		}catch(IOException e){
			System.out.println("Output File not found!");
		}
	}
	
	/* Returns all the document ids for the given term */
	private void getPostings(String term, LinkedList<Integer> postingsByDocID,LinkedList<Integer> postingsByTF){
		getPostingsByDocId(term,postingsByDocID);	
		getPostingsByTermFq(term,postingsByTF);
	}
	
	/* Helper function to get the postings by Doc ID */
	private void getPostingsByDocId(String term, LinkedList<Integer> postingsByDocID){
		if(!daat.containsKey(term)){
			return;
		}
		for(Postings post : daat.get(term).postingsList){
			postingsByDocID.add(post.Doc_id);
		}
	}
	
	/* Helper function to get the postings by Term Frequency */
	private void getPostingsByTermFq(String term, LinkedList<Integer> postingsByTF){
		if(!taat.containsKey(term)){
			return;
		}
		for(Postings post : taat.get(term).postingsList){
			postingsByTF.add(post.Doc_id);
		}
	}

	public static void main(String args[]){		
		String indexFile = args[0];
		String logFile = args[1];
		Integer topK = Integer.parseInt(args[2]);
		String queryFile = args[3];
		
		TaaT_DaaT obj = new TaaT_DaaT();
		obj.Indexing(indexFile, logFile);
	    
	    try{
		    FileReader queryReader = new FileReader(queryFile);
		    FileWriter writer = new FileWriter(logFile);
		    BufferedReader bufferReader = new BufferedReader(queryReader);
		    bufferWriter = new BufferedWriter(writer);
		    String line = null;
		    
		    bufferWriter.write("FUNCTION: getTopK " + args[2]);
		    bufferWriter.newLine();
	    	obj.getTopK(topK);
	    	
		    while((line = bufferReader.readLine())!=null){
		    	String[] terms = line.split(" ");
		    	String queryLine = line.replaceAll(" ", ", ");
		    	for(String str : terms){
		    		LinkedList<Integer> byDocId = new LinkedList<>();
		    		LinkedList<Integer> byTermFq = new LinkedList<>();
		    		obj.getPostings(str, byDocId, byTermFq);
		    		
		    		bufferWriter.write("FUNCTION: getPostings " + str);
		    		bufferWriter.newLine();
		    		bufferWriter.write("Ordered by doc IDs: ");
		    		if(!daat.containsKey(str)){
		    			bufferWriter.write("Terms not found");
		    		}
		    		else{
			    		for(Integer docId : byDocId){
			    	    	if(docId.equals(byDocId.getLast()))
			    	    		bufferWriter.write(docId + "");
			    	    	else
			    	    		bufferWriter.write(docId + ", ");
			    	    }
		    		}
		    	    bufferWriter.newLine();
		    	    bufferWriter.write("Ordered by TF: ");
		    	    if(!taat.containsKey(str)){
		    			bufferWriter.write("Terms not found");
		    		}
		    	    for(Integer termFq : byTermFq){
		    	    	if(termFq.equals(byTermFq.getLast())){
		    	    		bufferWriter.write(termFq + "");
		    	    	}
		    	    	else
		    	    		bufferWriter.write(termFq + ", ");
		    	    }
		    	    bufferWriter.newLine();
		    	}
		    	boolean andPossible = true;
		    	LinkedList<String> termToPass = new LinkedList<>();
		    	for(int i=0; i<terms.length;i++){
		    		//if the term is not indexed already, set andPossible as false, henceforth all AND operations will return null
		    		//also don't add the term in the terms list to be passed for OR operation.
		    		if(!taat.containsKey(terms[i])){
		    			andPossible = false;
		    			continue;
		    		}
		    		termToPass.addLast(terms[i]);
		    	}
		    	
		    	bufferWriter.write("FUNCTION: termAtATimeQueryAnd " + queryLine);
		    	bufferWriter.newLine();
		    	if(andPossible){ //
			    	obj.termAtATimeQueryAnd(termToPass,false);		    	
			    	obj.termAtATimeQueryAnd(termToPass,true);	//for optimization
		    	}else{
		    		bufferWriter.write("00 documents are found ");
					bufferWriter.newLine();
					bufferWriter.write("00 comparisons are made");
					bufferWriter.newLine();
					bufferWriter.write("0.0 seconds are used");
					bufferWriter.newLine();
					bufferWriter.write("Result: Term not found"); 
					bufferWriter.newLine();
		    	}
		    	
		    	bufferWriter.write("FUNCTION: termAtATimeQueryOr " + queryLine);
		    	bufferWriter.newLine();
		    	obj.termAtATimeQueryOr(termToPass, false);
		    	obj.termAtATimeQueryOr(termToPass, true); //for optimization
		    	
		    	bufferWriter.write("FUNCTION: docAtATimeQueryAnd " + queryLine);
		    	bufferWriter.newLine();
		    	if(andPossible){
		    		obj.docAtATimeQueryAnd(termToPass);
		    	}
		    	else{
		    		bufferWriter.write("00 documents are found ");
					bufferWriter.newLine();
					bufferWriter.write("00 comparisons are made");
					bufferWriter.newLine();
					bufferWriter.write("0.0 seconds are used");
					bufferWriter.newLine();
					bufferWriter.write("Result: Term not found"); 
					bufferWriter.newLine();
		    	}
		    			    	
		    	bufferWriter.write("FUNCTION: docAtATimeQueryOr " + queryLine);
		    	bufferWriter.newLine();
		    	obj.docAtATimeQueryOr(termToPass);
		    }  
		    bufferReader.close(); 
		    bufferWriter.close();
	    }catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + indexFile + "'");                
        }
		catch(IOException ex){
			System.out.println("Error reading file");
		}
	}
}
