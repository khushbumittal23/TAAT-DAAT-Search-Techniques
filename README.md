# TaaT-DaaT-Search-Techniques

Implemented Term-at-a-time and Document-at-a-time Search techniques.</br>
• In the index file (term.idx), Each line(posting) indicates the document ids a term is posted in. 
• Every posting has three values: the term, the size of the posting list and the posting list itself. Each posting is
of the form X\cY\mZ where X is the term, Y is the size of posting list and Z is the posting list
• The posting list itself is expressed as [a/b, c/d, e/f,...]. The square brackets denote the start and end of the list.
Each entry of the form x/y means the term occurs 'y' times in document id 'x'. 
• Sample_input contains few keywords written on each line.
• The application finds out: </br>
  the most frequently used terms (top-k terms where k will be given as input)</br>
  retrieves the postings list corresponding to each key word mentioned in the input file</br>
  Term-At-A-Time And/Or operation on the keywords present in each line of the input file
  Document-At-A-Time And/Or operation on the keywords present in each line of the input file
  
Run Configurations: </br>
  Import the project in eclipse. </br>
  Open run configurations, open arguments tab and type </br>
  term.idx sample_output.log any_number_e.g._10 sample_input.txt </br>
  
