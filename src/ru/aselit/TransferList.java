package ru.aselit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.org.glassfish.external.statistics.annotations.Reset;

import static ru.aselit.TransferItemStateEnum.*;

public class TransferList extends UpdateState {

	private final String FILE_NAME = "./transfer_list.ini";
	
	private List<TransferItem> items;
//	private ReentrantLock locker;
	
//	unique item id
	private long id;
	
	private static final Logger log = LogManager.getLogger(TransferList.class);
	
	/**
	 * 
	 */
	public TransferList() {
		
		super(false);
		items = new ArrayList<TransferItem>();
//		locker = new ReentrantLock();
		id = 0;
	}
	
	@Override
	public boolean isUpdated() {
		
		boolean updated = super.isUpdated();
		for (TransferItem item : items) {
			
			updated |= item.isUpdated();
		}
		
		return updated;
	}
	
	@Override
	public void resetUpdate() {
		
		super.resetUpdate();
		for (TransferItem item : items) {
			
			item.resetUpdate();
		}
	}
		
	/**
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 * @param needLock
	 * @return
	 */
	public synchronized TransferItem add(String sourceFile, String destinationFile) {
		
		return add(id++, sourceFile, destinationFile);
	}
	
	/**
	 * 
	 * @param index
	 * @param sourceFile
	 * @param destinationFile
	 * @param needLock
	 * @return
	 */
	public synchronized TransferItem add(long id, String sourceFile, String destinationFile) {
		
		TransferItem item = isItem(sourceFile, destinationFile);
		if (null == item) {
			
			item = new TransferItem(id, sourceFile, destinationFile);
			items.add(item);
			setUpdated();
		}
		return item;
	}
		
	/**
	 * 
	 * @param index
	 */
	public synchronized void remove(int index) {
		
		try {
			
			TransferItem item = get(index);
			if (null != item)
				item.interruptThread();
			
			items.remove(index);
			setUpdated();
			
		} catch (IndexOutOfBoundsException ex) {
			
		}
	}
	
	/**
	 * 
	 * @param index
	 */
	public synchronized void remove(long id) {
		
		for (TransferItem item : items) {
			
			if (item.getId() != id)
				continue;
			
			item.interruptThread();
			
			items.remove(item);
			setUpdated();
			break;
		}
	}
	
	/**
	 * 
	 */
	public synchronized boolean removeIsDone() {
		
		boolean res = false;
		for (int i = items.size() - 1; i >= 0; i--) {
			
			TransferItem item = get(i);
			if (!item.getState().equals(tisDone))
				continue;
			remove(i);
			res = true;
		}
		return res;
	}
	
	/**
	 * Get the total number of elements. 
	 * @return
	 */
	public synchronized int size() {
		
		return items.size();
	}
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public synchronized TransferItem get(int index) {
		
		try {
			
			return items.get(index);
		} catch (IndexOutOfBoundsException ex) {
			
			return null;
		}
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public synchronized TransferItem get(long id) {
		
		for (TransferItem item : items) {
			
			if (item.getId() != id)
				continue;
			return item;
		}
		return null;
	}
	
	/**
	 * 
	 */
	public synchronized void clear() {
		
		for (int i = items.size() - 1; i >= 0; i--) {
			
			if (log.isDebugEnabled())
				log.debug(String.format("Remove the item %d.", i));
			remove(i);
		}
	}
	
	/**
	 * Load the list of files.
	 */
	public synchronized void load() {
		
		int i;
		Properties props = new Properties();
		List<TransferItem> usedItems;
		long itemId;
		
		try {
			
			File file = new File(FILE_NAME);
			if (file.exists()) {
				
				InputStream stream = new FileInputStream(FILE_NAME);
				props.load(stream);
				
				usedItems = new ArrayList<TransferItem>();
				
//				try to get last file index
				String prop = props.getProperty("id");
				if (null != prop)
					id = new Long(prop).longValue();
				
				Enumeration<Object> keys = props.keys();
				while (keys.hasMoreElements()) {
					
					String key = (String) keys.nextElement();
					Pattern pattern = Pattern.compile("sourceFile(\\d+)");
					Matcher matcher = pattern.matcher(key);
					if (matcher.find()) {
						
						itemId = new Long(matcher.group(1)).longValue();
//						increase the last index of file if it less
						if (itemId > id)
							id = itemId;
						TransferItem item = add(itemId,
							props.getProperty("sourceFile".concat(matcher.group(1))),
							props.getProperty("destinationFile".concat(matcher.group(1))));
						if (null != item)
							usedItems.add(item);
					}
					
				}
				stream.close();
				
//				remove unused items from list
				for (i = items.size() - 1; i >= 0; i--) {
					
					if (-1 != usedItems.indexOf(items.get(i)))
						continue;
					remove(i);
				}
				
//				sort in ascending order of index
				Collections.sort(items, new Comparator<TransferItem>() {

					@Override
					public int compare(TransferItem item1, TransferItem item2) {
						
						return (new Long(item1.getId()).compareTo(item2.getId()));
					}
					
				});
			}
		} catch (IOException e) {
		
		}
	}
	
	/**
	 * Save the list of files. 
	 */
	public synchronized void save() {
		
		Properties props = new Properties();
		
		try {
			
			OutputStream stream = new FileOutputStream(FILE_NAME);

			props.setProperty("id", String.format("%d", id));
			for (TransferItem item : items) {
				
				props.setProperty(String.format("sourceFile%d", item.getId()), item.getSourceFile());
				props.setProperty(String.format("destinationFile%d", item.getId()), item.getDestinationFile());
			}
				
			props.store(stream, "");
			stream.close();
		} catch (IOException e) {
		
		}
	}
	
	/**
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 * @return
	 */
	public synchronized TransferItem isItem(String sourceFile, String destinationFile) {
		
		TransferItem cmp;
			
		try {
		
			cmp = new TransferItem(-1, sourceFile, destinationFile);
			for (TransferItem item : items) {
				
				if (item.equals(cmp))
					return item;
			}
			return null;
		} finally {
			
			cmp = null;
		}
	}
	
	/**
	 * Select a file from list.
	 * @return
	 */
	public synchronized TransferItem select() {
		
		for (TransferItem item : items) {
			
			if (tisNew == item.getState()) {
			
				setUpdated();
				
				File file = new File(item.getSourceFile());
				if (!file.exists())
					item.setState(tisError);
				else {
					item.setState(tisUpload);
					return item;
				}
			}
		}
		return null;
	}
}
