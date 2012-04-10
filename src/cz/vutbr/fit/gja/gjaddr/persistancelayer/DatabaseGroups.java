package cz.vutbr.fit.gja.gjaddr.persistancelayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with groups collection.
 *
 * @author Bc. Radek Gajdušek <xgajdu07@stud.fit.vutbr.cz>
 */
public class DatabaseGroups {
	
	private int idCounter = 0;
	
	private final String FILENAME = new File(Settings.getDataDir(), "groups.gja").toString();
	private ArrayList<Group> groups = null;

	public DatabaseGroups() {		
		this.load();
		this.setLastIdNumber();
	}	
	
	private void load()	{
		
		this.groups = null;
		
		if ((new File(FILENAME)).exists()) {
			try {
				FileInputStream flinpstr = new FileInputStream(FILENAME);
				ObjectInputStream objinstr= new ObjectInputStream(flinpstr);

				try {	
					this.groups = (ArrayList<Group>) objinstr.readObject(); 
				} 
				finally {
					try {
						objinstr.close();
					} 
					finally {
						flinpstr.close();
					}
				}
			} 
			catch(IOException ioe) {
				ioe.printStackTrace();
			} 
			catch(ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
		}		
		
		// create empty DB
		if (this.groups == null) {
			this.groups = new ArrayList<Group>();
		}			
	}
	
	private void setLastIdNumber() {
		
		int counter = 0;
		
		for (Group group: this.groups) {
			int id = group.getId();
			if (id > counter) {
				counter = id;
			}
		}
		
		this.idCounter = counter;
	}	
	
	private boolean checkNameIfExists(String name) {
		for (Group group: this.groups) {
			if (group.getName().equals(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	void save()	{
		
		if (this.groups == null || this.groups.isEmpty()) {
			return;
		}
		
		try {
			FileOutputStream flotpt = new FileOutputStream(FILENAME);
			ObjectOutputStream objstr= new ObjectOutputStream(flotpt);
			
			try {
				objstr.writeObject(this.groups); 
				objstr.flush();
			} 
			finally {				
				try {
					objstr.close();
				} 
				finally {
					flotpt.close();
				}
			}
		} 
		catch(IOException ioe) {
			ioe.printStackTrace();
		}		
	}
	
	void clear() {
		this.groups.clear();
		this.idCounter = 0;		
	}	
	
	public List<Group> getAllGroups() {
		return new ArrayList<Group>(this.groups);
	}

	/**
	 * Get group by it's name.
	 * 
	 * @param name
	 * @return
	 */
	public Group getGroupByName(String name) {
		for (Group g : this.groups) {
			if (g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}
	
	boolean addNew(String name) {
		
		if (this.checkNameIfExists(name)) {
			return false;
		}
		
		Group newGroup = new Group(++this.idCounter, name);
		this.groups.add(newGroup);
		
		return true;
	}
	
	void updateGroup(Group group) {
		Group updatedGroup = this.filterItem(group.getId());
		int index = this.groups.indexOf(updatedGroup);
		if (index != -1) {
			this.groups.set(index, group);
		}
	}
	
	void removeGroup(List<Group> groupsToRemove) {
		this.groups.removeAll(groupsToRemove);
	}
	
	private Group filterItem(int id) {
		for (Group group : this.groups) {			
			if (group.getId() == id) 
				return group;
			}

		return null;
	}	
	
	List<Group> filter(List<Integer> reguiredIdList) {
		
		List<Group> filteredGroups = new ArrayList<Group>();
		
		for (Group group : this.groups) {
			
			if (reguiredIdList.contains(group.getId()))
				filteredGroups.add(group);
			}

		return filteredGroups;
	}	
}
