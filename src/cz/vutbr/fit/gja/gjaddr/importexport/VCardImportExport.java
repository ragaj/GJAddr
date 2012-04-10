
package cz.vutbr.fit.gja.gjaddr.importexport;

import cz.vutbr.fit.gja.gjaddr.persistancelayer.Address;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Email;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.PhoneNumber;
import cz.vutbr.fit.gja.gjaddr.persistancelayer.Url;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.VCardWriter;
import net.sourceforge.cardme.vcard.VCardImpl;
import net.sourceforge.cardme.vcard.VCardVersion;
import net.sourceforge.cardme.vcard.features.AddressFeature;
import net.sourceforge.cardme.vcard.features.EmailFeature;
import net.sourceforge.cardme.vcard.features.NameFeature;
import net.sourceforge.cardme.vcard.features.NicknameFeature;
import net.sourceforge.cardme.vcard.features.NoteFeature;
import net.sourceforge.cardme.vcard.features.TelephoneFeature;
import net.sourceforge.cardme.vcard.features.URLFeature;
import net.sourceforge.cardme.vcard.types.AddressType;
import net.sourceforge.cardme.vcard.types.EmailType;
import net.sourceforge.cardme.vcard.types.FormattedNameType;
import net.sourceforge.cardme.vcard.types.NameType;
import net.sourceforge.cardme.vcard.types.NoteType;
import net.sourceforge.cardme.vcard.types.TelephoneType;
import net.sourceforge.cardme.vcard.types.URLType;
import net.sourceforge.cardme.vcard.types.VersionType;

import org.slf4j.LoggerFactory;

/**
 * Class for importing and exporting vCards.
 *
 * @author Bc. Drahomira Herrmannova <xherrm01@stud.fit.vutbr.cz>
 * @see <http://dma.pixel-act.com/>
 * @see <http://sourceforge.net/apps/mediawiki/cardme/index.php?title=Main_Page>
 */
public class VCardImportExport {

	/**
	 * Application database.
	 */
	private Database database = Database.getInstance();

	/**
	 * Read all contents of file as string.
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private String readFileAsString(String filePath) throws IOException {
		byte[] buffer = new byte[(int) new File(filePath).length()];
		FileInputStream f = new FileInputStream(filePath);
		f.read(buffer);
		return new String(buffer);
	}

	/**
	 * Split vcard file to single vcards.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String[] splitVcardFile(File file) throws IOException {
		List<String> parts = new ArrayList<String>();
		String fileAsString = this.readFileAsString(file.getAbsolutePath());
		while (fileAsString != null && fileAsString.contains("END:VCARD")) {
			parts.add(fileAsString.substring(0, fileAsString.indexOf("END:VCARD") + "END:VCARD".length()));
			fileAsString = fileAsString.substring(fileAsString.indexOf("END:VCARD") + "END:VCARD".length(),
					fileAsString.length());
		}
		return parts.toArray(new String[parts.size()]);
	}

	/**
	 * Get group by it's name.
	 * 
	 * @param groupName
	 * @return
	 */
	private Group getGroupByName(String groupName) {
		for (Group g : this.database.getAllGroups()) {
			if (g.getName().equals(groupName)) {
				return g;
			}
		}
		return null;
	}
	
	/**
	 * Import vCard file.
	 * 
	 * @param file
	 * @throws VCardException
	 */
	public void importContacts(File file) throws IOException {
		this.importContactsToGroup(file, null);
	}

	/**
	 * Import contacts from vCard file to group (specified by it's name).
	 *
	 * @param file
	 * @param group Name of group to import to.
	 * @throws VCardException
	 */
	public void importContactsToGroup(File file, String group) throws IOException {
		String[] vcardStrings = this.splitVcardFile(file);
		VCardEngine vcardEngine = new VCardEngine();
		VCard[] vcards = vcardEngine.parse(vcardStrings);
	
		// list of contacts to be imported to database
		List<Contact> contacts = new ArrayList<Contact>();

		// cycle through all nodes of the vcard and build contacts
		for (VCard vcard : vcards) {
			// get name
			NameFeature name = vcard.getName();

			// get nickname
			NicknameFeature nicknames = vcard.getNicknames();
			String nickname = null;
			if (nicknames != null) {
				Iterator<String> nicknamesIterator = nicknames.getNicknames();
				nickname = nicknamesIterator.hasNext() ? nicknamesIterator.next() : null;
			}
			
			// get note
			Iterator<NoteFeature> notesIterator = vcard.getNotes();
			String note = null;
			if (notesIterator != null) {
				note = notesIterator.hasNext() ? notesIterator.next().getNote() : null;
			}

			// get addresses
			Iterator<AddressFeature> addressesIterator = vcard.getAddresses();
			List<Address> addresses = new ArrayList<Address>();
			while (addressesIterator != null && addressesIterator.hasNext()) {
				// TODO post code
				AddressFeature af = addressesIterator.next();
				addresses.add(new Address(1, af.getStreetAddress(), af.getLocality(), 0, af.getCountryName()));
			}
			
			// get emails
			Iterator<EmailFeature> emailsIterator = vcard.getEmails();
			List<Email> emails = new ArrayList<Email>();
			while (emailsIterator != null && emailsIterator.hasNext()) {
				emails.add(new Email(1, emailsIterator.next().getEmail()));
			}
			
			// get phones
			Iterator<TelephoneFeature> telephoneNumbersIterator = vcard.getTelephoneNumbers();
			List<PhoneNumber> phones = new ArrayList<PhoneNumber>();
			while (telephoneNumbersIterator != null && telephoneNumbersIterator.hasNext()) {
				phones.add(new PhoneNumber(1, telephoneNumbersIterator.next().getTelephone()));
			}
			
			// get URLs
			Iterator<URLFeature> urlsIterator = vcard.getURLs();
			List<Url> urls = new ArrayList<Url>();
			while (urlsIterator != null && urlsIterator.hasNext()) {
				urls.add(new Url(1, urlsIterator.next().getURL()));
			}

			// build new contact
			Contact dbContact = new Contact(name.getGivenName(), name.getFamilyName(), nickname, note);
			dbContact.setEmails(emails);
			dbContact.setAdresses(addresses);
			dbContact.setPhoneNumbers(phones);
			dbContact.setUrls(urls);

			// add contact to list
			contacts.add(dbContact);
			LoggerFactory.getLogger(this.getClass()).debug("Adding contact [" 
					+ name.getGivenName() + " " + name.getFamilyName() + "].");
		}

		// save contacts in database
		if (group == null || group.isEmpty()) {
			LoggerFactory.getLogger(this.getClass()).debug("Group is empty.");
		} else {
			LoggerFactory.getLogger(this.getClass()).debug("Adding contacts to : " + group);
		}

		// first add contacts to database
		this.database.addNewContacts(contacts);

		// then add to group
		if (group != null) {
			this.database.addNewGroup(group);
			Group dbGroup = this.getGroupByName(group);
			if (dbGroup != null) {
				this.database.addContactsToGroup(dbGroup, contacts);
			}
		}
	}

	/**
	 * Export to vCard file
	 */
	public void exportContacts(File file, List<Contact> contacts) throws IOException {

		FileOutputStream fos = null;
		OutputStreamWriter osw = null;

		try {
			fos = new FileOutputStream(file);
			osw = new OutputStreamWriter(fos);

			VCardWriter writer = new VCardWriter();

			// create one card for each contact
			for (Contact c : contacts) {
				VCard vcard = new VCardImpl();
				vcard.setVersion(new VersionType(VCardVersion.V3_0));

				// set contact name
				NameType name = new NameType();
				name.setFamilyName(c.getSurName());
				name.setGivenName(c.getFirstName());
				vcard.setName(name);
				vcard.setFormattedName(new FormattedNameType(c.getFullName()));

				// set contact addresses
				for (Address address : c.getAdresses()) {
					AddressFeature af = new AddressType();
					af.setCountryName(address.getCountry());
					af.setLocality(address.getCity());
					af.setStreetAddress(address.getStreet() + " " + address.getNumber());
					af.setPostalCode(address.getPostCodeAsString());
					// TODO set type
					vcard.addAddress(af);
				}

				// set contact emails
				for (Email email : c.getEmails()) {
					EmailFeature ef = new EmailType();
					ef.setEmail(email.getEmail());
					// TODO set type
					vcard.addEmail(ef);
				}

				// set contact note
				if (c.getNote() != null && !c.getNote().isEmpty()) {
					NoteFeature note = new NoteType();
					note.setNote(c.getNote());
					vcard.addNote(note);
				}

				// set contact phones
				for (PhoneNumber phone : c.getPhoneNumbers()) {
					TelephoneFeature tf = new TelephoneType();
					tf.setTelephone(phone.getNumber());
					vcard.addTelephoneNumber(tf);
				}

				// set contact URLs
				for (Url url : c.getUrls()) {
					vcard.addURL(new URLType(url.getValue()));
				}

				//create vCard representation
				writer.setVCard(vcard);
				String vcardString = writer.buildVCardString();

				//write vCard to the output stream
				osw.write(vcardString);
				osw.write("\n"); //add empty lines between contacts
			}
		} finally {
			try {
				osw.close();
			} finally {
				fos.close();
			}
		}
	}

	/**
	 * Test the class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			VCardImportExport vc = new VCardImportExport();
			vc.importContacts(new File("/Users/damirah/Downloads/contacts.vcf"));
		} catch (IOException ex) {
			Logger.getLogger(VCardImportExport.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}