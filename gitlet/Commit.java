package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Date;
import static gitlet.Utils.*;
import java.text.SimpleDateFormat;

/**
 * @author Truong Le
 */
public class Commit implements Serializable {
    private static final long serialVersionUID = 123L;
    /** Commit message. */
    private String _message;
    /** Commit time. */
    private Date _timestamp;
    /** Commit parent. */
    private Commit _parent;
    /** Commit ID: its SHA? */
    private String _commitID;
    /** Commit blobs: Key: Filename ; Val: SHA. */
    private HashMap<String, String> _blobsPtr;
    /** Commit parent1 for merge. */
    private Commit _mergedParent1 = null;
    /** Commit parent2 for merge. */
    private Commit _mergedParent2 = null;

    /** Commit Constructor.
     * @param message  the message of commit
     * @param parent   the parrent of commit
     * @param blobsPtr the files of commit
     */
    public Commit(String message, Commit parent,
                  HashMap<String, String> blobsPtr) {
        this._message = message;
        this._parent = parent;
        this._blobsPtr = new HashMap<String, String>();

        if (this._parent == null) {
            final long magic = 3600000L * 8;
            this._timestamp = new Date(magic);
            for (String blobName: blobsPtr.keySet()) {
                this._blobsPtr.put(blobName, blobsPtr.get(blobName));
            }
        } else {
            this._timestamp = new Date();
            for (String blobName: blobsPtr.keySet()) {
                this._blobsPtr.put(blobName, blobsPtr.get(blobName));
            }
        }
    }

    /** Restore Blobs to this commit's version.
     * @param fileName the name of file wants to recover.
     * @param blobSHA the blobSHA of the file. */
    public void recover(String fileName, String blobSHA) {
        File thisBlobfile = new File(Main.BLOBS_DIR.getPath()
                + "/" + blobSHA);

        if (!thisBlobfile.exists()) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeContents(new File(fileName), readContents(thisBlobfile));
    }

    /** Return the length between THIS and INIT commit.
     * @return the distance from INIT. RECURSIVELY*/
    public int distanceToInit() {
        if (_parent == null) {
            return 0;
        } else {
            return 1 + getParent().distanceToInit();
        }
    }

    /** Get the kth commit from this RECURSIVELY.
     * @param k the Kth commit from this.
     * @return A COMMIT distance Kth from this. */
    public Commit far(int k) {
        if (k < 0) {
            System.out.println("No negative distance");
            System.exit(0);
        }
        if (k == 0) {
            return this;
        }
        return far(k - 1).getParent();
    }

    /** Return the Commit Message. */
    public String getMessage() {
        return this._message;
    }

    /** Return the Commit Time. */
    public Date getTimestamp() {
        return this._timestamp;
    }

    /** Return the Commit Parent, it exists. */
    public Commit getParent() {
        return this._parent;
    }

    /** Return the Commit ID.
     * NOT WORK, NOT SURE WHY, RETURN ADDRESS
     * IN MEMORY OF COMMIT, NOT ITS SHA???*/
    public String getCommitID() {
        return this._commitID;
    }

    /** Return the SHA-1 hash value of this Commit. */
    public String getCommitSHA() {
        return sha1(serialize(this));
    }

    /** Return the SHA-1 hash value of a Blob File in this Commit.
     * @param fileName name of file to get Blob from. */
    public String getBlobSHA(String fileName) {
        return _blobsPtr.get(fileName);
    }

    /** Return the Blobs Pointers . */
    public HashMap<String, String> getBlobsPtr() {
        return this._blobsPtr;
    }

    /** Compare 2 commits.
     * @param c the commit to compare with THIS commit.
     * @return TRUE if the commit is the same using SHA. */
    public boolean equals(Commit c) {
        return this.getCommitSHA().equals(c.getCommitSHA());
    }

    /** Return merged Parent1. */
    public Commit getMergedParent1() {
        return _mergedParent1;
    }
    /** Set SHA of merged Parent1.
     * @param p1 the SHA code of parent1. */
    public void setMergedParent1(Commit p1) {
        _mergedParent1 = p1;
    }

    /** Return merged Parent2. */
    public Commit getMergedParent2() {
        return _mergedParent2;
    }
    /** Set SHA of merged Parent2.
     * @param p2 the SHA code of parent1. */
    public void setMergedParent2(Commit p2) {
        _mergedParent2 = p2;
    }

    /** Get the log (History) of the commit.
     * "EEE, d MMM yyyy HH:mm:ss Z" Wed, 4 Jul 2001 12:08:56 -0700
     * Tue 21 Apr 2020 21:14:07 -0700
     * docs.oracle.com/javase/10/docs/api/java/text/SimpleDateFormat.html
     * stackoverflow.com/questions/12781273/what-are-the-date-formats-
     * available-in-simpledateformat-class
     * "yyyy-MM-dd'T'HH:mm:ssZ" <=> 2013-09-29T18:46:19-0700
     * */
    public void log() {
        Date date = this.getTimestamp();
        String msg = this.getMessage();

        System.out.println("===");
        System.out.println("commit " + this.getCommitSHA());

        if (this.getMergedParent1() != null
                && this.getMergedParent2() != null) {
            String p1SHA = this.getMergedParent1().getCommitSHA().
                    substring(0, 7);
            String p2SHA = this.getMergedParent2().getCommitSHA().
                    substring(0, 7);
            System.out.println("Merge: " + p1SHA + " " + p2SHA);
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z\n");
        System.out.print("Date: " + dateFormatter.format(date));
        System.out.println(msg);
        System.out.println();
    }

}
