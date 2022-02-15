# Metalnx: A Brief History

10/2017

Steve Worth

##### The Start

In late 2014 I was approached by another EMC colleague, Sue Smith, to help investigate how metadata was evolving and could be applied to Big Data management beyond  tagging and searching engine use.

As our investigations progressed we discovered that system generated metadata and some fixed structure user and machine generated metadata were being used for indexing, duplicate detecting, and data compliance. What also found that little was happening to use user defined metadata to add data context and value information - something we felt was key to eventual multi-generational use of Big Data. 

We changed the focus of our work to look at tools, companies, and research work that could leverage user defined metadata as we felt that is where the future potential lay.  We found several areas and tools of interest, but one stood out as being the likely "best way forward".  That tool (or framework) was the Integrated Rule Oriented Data System (iRODS for short) provided by the iRODS Consortia which is part of the RENCI Institute (Renaissance Computing Institute) at the University of North Carolina Chapel Hill.

We met the iRODS developers (a fantastic group of software developers), learned about the product, and became convinced this was where to focus our learning. 

Along the way we were introduced to Sasha Paegle and Patrick Combes who were part of the Isilon Life Sciences Product team at the time and who both have deep experience with genomics and bioinformatics research and work constantly with Big Data. We learned from them how user defined metadata was used in Life Science research to provide phenotype information which is critical to genomics research. 

##### The Idea
As the four of us worked more closely we learned some important things:

* Most Life Science research groups struggle with adding contextual metadata to their data sources.
* While iRODS is used by many Life Science research teams many avoid it because they see it as complicated to deploy and use.
* Most researchers know Linux and some level of programming because is it necessary.
* These same researchers would prefer the computing be a tool (like a microscope) vs. a time consuming activity for them.
* The rate of growth in Big Data sets in genomics research is quickly consuming significant portions of the research IT budget.
* Most research groups want a level of structure in their long term data sets, but require flexibility to add context information that describes changing and varied patient contexts.

These insights became guides as we moved forwards in our efforts. 

During 2015 we were fortunate and had opportunity to talk with many of our Life Science IT customers, attend BioIT World, and the annual iRODS User Group Meeting. 

We came away understanding the following:
* Working with Life Science customers and the iRODS user community was the best way observe how user defined metadata will evolve.
* The iRODS user community is very technical because most are early adopters.
* User defined metadata (and iRODS) may struggle to grow and evolve because the entry barrier (technically) is too steep for some organizations.

The greatest value we could add would be to simplify the administration of a system for and the management of user defined metadata.  This notion is what started the creation of Metalnx.

##### Creating Metalnx

In 2015 we secured funding to design a tool that would make it easier for our customers to adopt iRODS and apply it, along with user defined metadata, to their research efforts.

We assembled a team of four design developers in Brazil with our development partner, Instituto de Pesquisas Eldorado, who began work.  The main design principles used were:

* Development tools & libraries should support web & mobile devices from the same application.
* The implementation should be scalable via virtualization
* The tool must live along side of iRODS.
* To the extent possible the tool should have light dependencies to iRODS.
* All tools / libraries used should be open source with BSD or GPL2 type licenses to have minimal risk should this tool also be put into open source.

The development team of 
Henrique Nogueira, 
Arthur Guerra,
Mileine Assato,
& Ng Yen
worked in late 2015 & 2016 define and build the user application using an agile approach based on user usage scenarios which were developed from interviews with genomics researchers.

As each portion (feature) of Metalnx was completed we demonstrated it with likely users taking feedback and making adjustments based on feedback.

A beta program was put together in 2016 where users tried out the application, provided feedback, and improvements were made based on the feedback.

##### Delivering Metalnx

As development continued a number of decisions about delivery were made.  These included:

* Metalnx should be put into open source to encourage usage and future participation in its evolution.

* The application is delivered in source code on GitHub and as a docker image on Docker Hub.

Metalnx was first delivered for general use in late 2016 and with new features / improvements during the first half of 2017.  


##### Recent Events
In the spring of 2017 a team of students at North Carolina State University created an initial implementation of user rule deployment via Metalnx.  This initial implementation was enhanced and pulled into the 1.4 release.  

We wish to acknowledge the student team of Madison Hayes, Daniel Gross, Richard Pajerksi, Andrew Losi, and their faculty advisor Dr. David Sturgill for their efforts in creating the rule deployment implementation as part of their Senior Design project.

Following delivery of version 1.4 in mid-2017 the development team disbanded to support other important internal projects.

Metalnx was put into open source by Dell EMC to help encourage greater use of metadata and iRODS for research purposes.  We hope that researchers and data archivist will consider the tool, leverage it, and contribute to its future development.

