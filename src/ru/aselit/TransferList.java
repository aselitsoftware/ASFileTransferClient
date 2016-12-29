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

import static ru.aselit.TransferItemStateEnum.*;

public class TransferList {

	private final String FILE_NAME = "./transfer_list.ini";
	
	private List<TransferItem> items;
	private ReentrantLock locker;
	private boolean updated;
//	unique index
	private int index;
	
	private static final Logger log = LogManager.getLogger(TransferList.class);
	
	/**
	 * 
	 */
	public TransferList() {
		
		items = new ArrayList<TransferItem>();
		locker = new ReentrantLock();
		updated = false;
		index = 0;
	}
	
	/**
	 * 
	 * @return true or false
	 */
	public boolean isUpdated() {
		
		return updated;
	}
	
	public void resetUpdate() {
		
		updated = false;
	}
		
	
	/**
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 * @param needLock
	 * @return
	 */
	public TransferItem add(String sourceFile, String destinationFile, boolean needLock) {
		
		return add(index++, sourceFile, destinationFile, needLock);
	}
	
	/**
	 * 
	 * @param index
	 * @param sourceFile
	 * @param destinationFile
	 * @param needLock
	 * @return
	 */
	public TransferItem add(int index, String sourceFile, String destinationFile, boolean needLock) {
		
		try {
			
			if (needLock)
				locker.lock();
			
			TransferItem item = isItem(sourceFile, destinationFile, false);
			if (null == item) {
				
				item = new TransferItem(index, sourceFile, destinationFile);
				items.add(item);
				updated = true;
			}
			return item;
			
		} finally {
			
			if (needLock)
				locker.unlock();
		}
	}
		
	/**
	 * 
	 * @param index
	 */
	public void removeByListIndex(int index, boolean needLock) {
		
		try {
			
			if (needLock)
				locker.lock();
			try {
				
				TransferItem item = getByListIndex(index, false);
				if (null != item)
					item.interruptThread();
				
				items.remove(index);
				updated = true;
				
			} catch (IndexOutOfBoundsException ex) {
				
			}
		} finally {
			
			if (needLock)
				locker.unlock();
		}
	}
	
	/**
	 * 
	 * @param index
	 */
	public void removeByItemIndex(int index, boolean needLock) {
		
		try {
			
			if (needLock)
				locker.lock();
			for (TransferItem item : items) {
				
				if (item.getIndex() != index)
					continue;
				
				item.interruptThread();
				
				items.remove(item);
				updated = true;
				break;
			}
			
		} finally {
			
			if (needLock)
				locker.unlock();
		}
	}
	
	/**
	 * Get the total number of elements. 
	 * @return
	 */
	public int size() {
		
		try {
		
			locker.lock();
			return items.size();
		} finally {
			
			locker.unlock();
		}
	}
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public TransferItem getByListIndex(int index, boolean needLock) {
		
		try {
			
			if (needLock)
				locker.lock();
			try {
				
				return items.get(index);
			} catch (IndexOutOfBoundsException ex) {
				
				return null;
			}
				
		} finally {
			
			if (needLock)
				locker.unlock();
		}
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public TransferItem getByItemIndex(int index) {
		
		try {
			
			locker.lock();
			for (TransferItem item : items) {
				
				if (item.getIndex() != index)
					continue;
				return item;
			}
			return null;
			
		} finally {
			
			locker.unlock();
		}
	}
	
	/**
	 * 
	 */
	public void clear() {
		
		try {
		
			locker.lock();
			for (int i = items.size() - 1; i >= 0; i--) {
				
				if (log.isDebugEnabled())
					log.debug(String.format("Remove the item %d.", i));
				removeByListIndex(i, false);
			}
		} finally {
			
			locker.unlock();
		}
	}
	
	/**
	 * Load the list of files.
	 */
	public void load() {
		
		int i;
		Properties props = new Properties();
		List<TransferItem> usedItems;
		int itemIndex;
		
		try {
			
			locker.lock();
			File file = new File(FILE_NAME);
			if (file.exists()) {
				
				InputStream stream = new FileInputStream(FILE_NAME);
				props.load(stream);
				
				usedItems = new ArrayList<TransferItem>();
				
//				try to get last file index
				String prop = props.getProperty("index");
				if (null != prop)
					index = new Integer(prop).intValue();
				
				Enumeration<Object> keys = props.keys();
				while (keys.hasMoreElements()) {
					
					String key = (String) keys.nextElement();
					Pattern pattern = Pattern.compile("sourceFile(\\d+)");
					Matcher matcher = pattern.matcher(key);
					if (matcher.find()) {
						
						itemIndex = new Integer(matcher.group(1)).intValue();
//						increase the last index of file if it less
						if (itemIndex > index)
							index = itemIndex;
						TransferItem item = add(itemIndex,
							props.getProperty("sourceFile".concat(matcher.group(1))),
							props.getProperty("destinationFile".concat(matcher.group(1))),
							false);
						if (null != item)
							usedItems.add(item);
					}
					
				}
				stream.close();
				
//				remove unused items from list
				for (i = items.size() - 1; i >= 0; i--) {
					
					if (-1 != usedItems.indexOf(items.get(i)))
						continue;
					removeByListIndex(i, false);
				}
				
//				sort in ascending order of index
				Collections.sort(items, new Comparator<TransferItem>() {

					@Override
					public int compare(TransferItem item1, TransferItem item2) {
						
						return (new Integer(item1.getIndex()).compareTo(item2.getIndex()));
					}
					
				});
			}
		} catch (IOException e) {
		
		} finally {
			
			usedItems = null;
			locker.unlock();
		}
	}
	
	/**
	 * Save the list of files. 
	 */
	public void save() {
		
		Properties props = new Properties();
		
		try {
			
			locker.lock();
			OutputStream stream = new FileOutputStream(FILE_NAME);

			props.setProperty("index", String.format("%d", index));
			for (TransferItem item : items) {
				
				props.setProperty(String.format("sourceFile%d", item.getIndex()), item.getSourceFile());
				props.setProperty(String.format("destinationFile%d", item.getIndex()), item.getDestinationFile());
			}
				
			props.store(stream, "");
			stream.close();
		} catch (IOException e) {
		
		} finally {
			
			locker.unlock();
		}
	}
	
	/**
	 * 
	 * @param sourceFile
	 * @param destinationFile
	 * @return
	 */
	public TransferItem isItem(String sourceFile, String destinationFile, boolean needLock) {
		
		TransferItem cmp;
		try {

			if (needLock)
				locker.lock();
			cmp = new TransferItem(-1, sourceFile, destinationFile);
			for (TransferItem item : items) {
				
				if (item.equals(cmp))
					return item;
			}
			return null;
		} finally {
			
			cmp = null;
			if (needLock)
				locker.unlock();
		}
	}
	
	/**
	 * Select a file from list.
	 * @return
	 */
	public TransferItem select() {
		
		try {
			
			locker.lock();
			for (TransferItem item : items) {
				
				if (tisNew == item.getState()) {
				
					updated = true;
					
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
		} finally {
			
			locker.unlock();
		}
	}
}
