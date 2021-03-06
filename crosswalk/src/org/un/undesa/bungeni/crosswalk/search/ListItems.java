package org.un.undesa.bungeni.crosswalk.search;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.search.DSQuery;
import org.dspace.search.Harvest;
import org.dspace.search.HarvestedItemInfo;
import org.dspace.content.*;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringEscapeUtils;

public class ListItems extends HttpServlet {
	
	protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
		System.out.println("\n\nreached listItems");
		
		String hand = null;
		String start = null;
		String limit = null;
		try {
			hand = request.getParameter("handle");			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		try {
			start = request.getParameter("start");
			//limit = request.getParameter("limit");
		}catch(Exception e) {
			start="0";
			e.printStackTrace();
		}
		
		SearchResults sr = new SearchResults();
		List<SearchResult> lsr = new ArrayList<SearchResult>();
		if(hand==null || hand=="") {
			System.out.println("\n\nNo handle supplied!!");		
			
			sr.setStartRecord(Integer.valueOf(start));
			sr.setMaxPages(0);
			sr.setMaxResults(0);
			sr.setPageSize(0);
			sr.setSearchResults(lsr);
		}else {
			DSpaceObject resultDSO=null;
			try {
				resultDSO=HandleManager.resolveToObject(getContext(request), hand);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			sr = new SearchResults();
			lsr = new ArrayList<SearchResult>();
			
			switch(resultDSO.getType()) {
		    	case Constants.ITEM:
		    		System.out.println("\n\nin item"+Constants.ITEM);
		    		System.out.println("\n\ntype is:"+resultDSO.getType()+"   handle is:"+resultDSO.getHandle());
		    		
		    		break;
		    	case Constants.COLLECTION:
		    		System.out.println("\n\nin collection"+Constants.COLLECTION);
		    		org.dspace.content.Collection col = (org.dspace.content.Collection) resultDSO;
		    				
		    		sr = compileCollection(col, request);
		    		break;
		    	case Constants.COMMUNITY:
		    		System.out.println("\n\nin community"+Constants.COMMUNITY);
		    		org.dspace.content.Community com = (org.dspace.content.Community) resultDSO;
		    		SearchResults srest = null;
		    		try {
		    			if(com.getSubcommunities().length!=0) {
		    				System.out.println("\n\nIn a top-level community");
		    				for(Community cm:com.getSubcommunities()) {
		    					for(org.dspace.content.Collection coln:cm.getCollections()) {
									SearchResults srt = compileCollection(coln, request);
									srest = unify(srt, srest);						
								}
		    				}
		    			}else {
		    				for(org.dspace.content.Collection coln:com.getCollections()) {
								SearchResults srt = compileCollection(coln, request);
								srest = unify(srt, srest);						
							}
		    			}	    			
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					sr=srest;
		    		break;
		    	default:
		    		System.out.println("\n\nNone of the above:"+Constants.ITEM);
		    		break;
	    	
			}
	        
			sr.setStartRecord(Integer.valueOf(start));
		}
		
		
		List<SearchResult> ds = sr.getSearchResults();
		List<SearchResult> ds2 = new ArrayList<SearchResult>();
		int counter = Integer.valueOf(start);
		int count2 = 0;
		int count3 = 0;
		try {
			for(SearchResult fr: ds) {
				if(count2 == 20) {
					break;
				}			
				if(counter < count3) {
					count2++;
					ds2.add(fr);
				}
				count3++;
			}
		}catch(Exception e) {
			ds2 = ds;
		}
		
		sr.setSearchResults(ds2);
		
    	XStream xstream = new XStream();
        String xml = xstream.toXML(sr);
    	
        System.out.println("\n\n\nExtractRecord doGet ->"+xml);
        
        byte b[]= xml.getBytes();
        //byte c[] = new byte[100];
        //byte b2[]= xml.getBytes();
        System.out.println("xml length is -> "+xml.length());
        if(xml.length()==7) {
        	System.out.println("\n\nxml 7");
        }
        ByteArrayInputStream bif= new ByteArrayInputStream(b/*2*/);
        System.out.println("\n\n\nxml2");
        OutputStream stream=response.getOutputStream();
        System.out.println("\n\n\nxml3");
        response.setContentType("text/xml");
        System.out.println("\n\n\nxml1");
        
        int nextChar;
        while ( ( nextChar = bif.read() ) != -1  ) {
        	//System.out.println("\n\n\nxml-w1");
        	stream.write(nextChar);
        	//stream.write( '\n' );
        	stream.flush();
        }
        System.out.println("\n\n\nxml4");
        if(bif!=null)bif.close();
        System.out.println("\n\n\nxml6");
        if(stream!=null) stream.close();
        System.out.println("\n\n\nxml5");
	}
	
	private Context getContext(HttpServletRequest request) throws Exception{
        //Context context = null;

        // set all incoming encoding to UTF-8
        request.setCharacterEncoding("UTF-8");
        
        Context c = (Context) request.getAttribute("dspace.context");        

        if (c == null)
        {
            // No context for this request yet
            c = new Context();
            HttpSession session = request.getSession();

            // See if a user has authentication
            Integer userID = (Integer) session.getAttribute(
                    "dspace.current.user.id");
            // Set the session ID and IP address
            c.setExtraLogInfo("session_id=" + request.getSession().getId() + ":ip_addr=" + request.getRemoteAddr());

            // Store the context in the request
            request.setAttribute("dspace.context", c);
        }

        return c;
	}
	
	protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
		
    	doGet(request, response);
    }
	
	private List<CommunityCollection> collect(Community[] communities) throws Exception {
		List<CommunityCollection> ccl = new ArrayList<CommunityCollection>();
		      
        for(Community cm : communities) {
        	//comm.add(cm);
        	CommunityCollection cc = new CommunityCollection();
        	cc.setCommunityHandle(cm.getHandle());
        	cc.setCommunityName(cm.getMetadata("name"));
        	List<CommunityCollection> lcom = new ArrayList<CommunityCollection>();
        	for(org.dspace.content.Community cn: cm.getSubcommunities()) {
        		CommunityCollection collect = new CommunityCollection();
        		collect.setCommunityHandle(cn.getHandle());
        		collect.setCommunityName(cn.getMetadata("name"));
        		
        		List<CollectionCollection> lcol = new ArrayList<CollectionCollection>();
        		for(org.dspace.content.Collection cn2: cn.getCollections()) {
        			CollectionCollection col = new CollectionCollection();
            		col.setCollectionHandle(cn2.getHandle());
            		col.setCollectionName(cn2.getMetadata("name"));
            		lcol.add(col);
    	    	}
        		collect.setCollection(lcol); 
        		lcom.add(collect);
	    	}
        	cc.setCommunity(lcom);
        	ccl.add(cc);        	
        }
        return ccl;
	}
	
	private SearchResults unify(SearchResults sr1, SearchResults sr2) {
		if(sr1==null && sr2==null) {
			System.out.println("\n\nsr1&2=null");
			return null;
		}else if(sr1==null) {
			System.out.println("\n\nsr1=null");
			return sr2;
		}else if(sr2==null) {
			System.out.println("\n\nsr2=null");
			return sr1;
		}else {
			System.out.println("\n\nAbout to unionize collections");
			List<SearchResult> result = null;
			try {
				result= (List<SearchResult>) CollectionUtils.union(sr1.getSearchResults(), sr2.getSearchResults());
			}catch(Exception ex) {
				System.out.println("\n\nException @ unify");
				ex.printStackTrace();
				//return null;
			}

			System.out.println("\n\nafter catcher");
			SearchResults sr = new SearchResults();
			sr.setMaxPages(sr1.getMaxPages());
			sr.setMaxResults(sr1.getMaxResults()+sr2.getMaxResults());
			sr.setPageSize(sr1.getPageSize());
			sr.setSearchResults(result==null? new ArrayList<SearchResult>():result);
			sr.setStartRecord(sr1.getStartRecord()==0?1:sr1.getStartRecord());
			return sr;
		}
	}
	
	private SearchResults compileCollection(org.dspace.content.Collection col, HttpServletRequest request) {
		int count = 0;  
		ItemIterator ir = null;
		SearchResults sr = new SearchResults();
		List<SearchResult> lsr = new ArrayList<SearchResult>();
		try {
			ir= col.getAllItems();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			
			while(ir.hasNext()) {
				Item item = (Item) ir.next();
				
				SearchResult rest = new SearchResult();
				String colz = "";
		    	  Collection[] cln = null;
		    	  try {
		    		  cln =item.getCollections();
		    	  }catch(Exception ex) {
		    		  
		    	  }
		    	  for(Collection colln:cln) {
		    		  colz = colln.getMetadata("name");
		    	  }
		    	  rest.setCollection(colz);
			  DCDate dd = null;
			  DCValue val = null;
			  DCValue[] dcvv = item.getMetadata("dc", "date", "issued", Item.ANY);
			  if(dcvv.length > 0) {
				  dd = new DCDate(dcvv[0].value);
				  rest.setIssue_date(dd.toDate());
			  }
			  dcvv = item.getMetadata("dc", "contributor", "author", Item.ANY);
			  if(dcvv.length > 0) {
				  String str[]= {""};
				  try {
					  for(int i=0;i<dcvv.length;i++) {
						  str[i]=dcvv[i].value;  
					  }	   
				  }catch(Exception ex) {
					  ex.printStackTrace();
				  }	    		       		
				  rest.setAuthors(str);
			  }
			  dcvv = item.getMetadata("dc", "title", Item.ANY, Item.ANY);
			  if(dcvv.length > 0) {
				  rest.setTitle(dcvv[0].value);
			  }
			  dcvv = item.getMetadata("dc", "description", Item.ANY, Item.ANY);
			  if(dcvv.length > 0) {
				  String str[]= {""};
				  try {
					  for(int i=0;i<dcvv.length;i++) {
						  str[i]=StringEscapeUtils.escapeXml(dcvv[i].value); 
						  System.out.println("\n\n\ndc.decsrpition is:"+str[i]);
					  }	
				  }catch(Exception ex) {
					  ex.printStackTrace();
				  }	    		   
				  rest.setDetails(str);
			  }
			  dcvv = item.getMetadata("dc", "identifier", "uri", Item.ANY);
			  if(dcvv.length > 0) {
				  String str[] = dcvv[0].value.split("/");
					rest.setUrl(str[3]+"/"+str[4]);
			  }
			  
			  try {
	    		  Bundle[] bundles = item.getBundles("ORIGINAL");
	
		          	if (bundles.length == 0)
		          	{
		          		
		          	}
		          	else
		          	{
		          		boolean html = false;
		        		String handle = item.getHandle();
		        		Bitstream primaryBitstream = null;

		        		Bundle[] bunds = item.getBundles("ORIGINAL");
		        		Bundle[] thumbs = item.getBundles("THUMBNAIL");
		          	// if item contains multiple bitstreams, display bitstream
		        		// description
		        		boolean multiFile = false;
		        		Bundle[] allBundles = item.getBundles();

		        		for (int i = 0, filecount = 0; (i < allBundles.length)
		                    	&& !multiFile; i++)
		        		{
		        			filecount += allBundles[i].getBitstreams().length;
		        			multiFile = (filecount > 1);
		        		}

		        		/*
		        		// check if primary bitstream is html
		        		if (bunds[0] != null)
		        		{
		        			Bitstream[] bits = bunds[0].getBitstreams();

		        			for (int i = 0; (i < bits.length) && !html; i++)
		        			{
		        				if (bits[i].getID() == bunds[0].getPrimaryBitstreamID())
		        				{
		        					html = bits[i].getFormat().getMIMEType().equals(
		        							"text/html");
		        					primaryBitstream = bits[i];
		        				}
		        			}
		        		}*/
		        		List<TransferMap> ltm = new ArrayList<TransferMap>();
		        			for (int i = 0; i < bundles.length; i++)
		            		{
		        				System.out.println("fsdfws\n"+request.getServletPath()+"  "+request.getRequestURL()+"  "+request.getRequestURI() +"  "+request.getContextPath());
		        				String url = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/"));
		        				System.out.println(url);
		        				
		            			Bitstream[] bitstreams = bundles[i].getBitstreams();

		            			for (int k = 0; k < bitstreams.length; k++)
		            			{
		            				TransferMap tm = new TransferMap();
		            				// Skip internal types
		            				if (!bitstreams[k].getFormat().isInternal())
		            				{

		                                // Work out what the bitstream link should be
		                                // (persistent
		                                // ID if item has Handle)
		                                String bsLink = "<a target=\"_blank\" href=\""
		                                        + /*request.getContextPath()*/url;

		                                if ((handle != null)
		                                        && (bitstreams[k].getSequenceID() > 0))
		                                {
		                                    bsLink = bsLink + "/bitstream/"
		                                            + item.getHandle() + "/"
		                                            + bitstreams[k].getSequenceID() + "/";
		                                }
		                                else
		                                {
		                                    bsLink = bsLink + "/retrieve/"
		                                            + bitstreams[k].getID() + "/";
		                                }

		                                bsLink = bsLink
		                                        + UIUtil.encodeBitstreamName(bitstreams[k]
		                                            .getName(),
		                                            Constants.DEFAULT_ENCODING) + "\">";

		            					bsLink += bitstreams[k].getName()+" "+(bitstreams[k].getDescription() == null? "":bitstreams[k].getDescription())+"  "+UIUtil.formatFileSize(bitstreams[k].getSize())+"  "+bitstreams[k].getFormatDescription()+"</a>";
		            					tm.setKey(bsLink);
		            					ltm.add(tm);
		            				}
		            			}		            			
		            		}
		        			rest.setBitstream(ltm);
		        		
		          	}
	    	  }catch(Exception ex) {
	    		  ex.printStackTrace();	    		  
	    	  }
			  
			    count++;
			    lsr.add(rest);
			    
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(lsr.isEmpty()) {
			lsr.add(new SearchResult());
			sr.setSearchResults(lsr);
			sr.setMaxPages(0);
			sr.setMaxResults(0);
			sr.setPageSize(/**/20);
			sr.setStartRecord(/**/0);
		}else {
			sr.setSearchResults(lsr);
			sr.setMaxPages(count);
			sr.setMaxResults(count);
			sr.setPageSize(/**/20);
			sr.setStartRecord(/**/0);
		}
		return sr;
	}
}
