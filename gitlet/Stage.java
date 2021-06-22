package gitlet;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.io.File;
import static gitlet.Utils.*;

/**
 * @author Truong Le
 */
public class Stage implements Serializable {
    /** PERSISTENCE: Blobs to be added. Key: blob's name; value: blob's SHA */
    private HashMap<String, String> _stagedForAdditionBlobs;
    /** Blobs tracked to be removed.*/
    private HashSet<String> _stagedRemovalBlobs;
    /** Blobs to be commited this time. Key: blob's name; value: blob's SHA*/
    private HashMap<String, String> _allBlobsToBeCommited;
    /** Head pointer of current commit. */
    private Commit _head;

    /** State Constructor. */
    public Stage() {
        _stagedForAdditionBlobs = new HashMap<String, String>();
        _allBlobsToBeCommited = new HashMap<String, String>();
        _stagedRemovalBlobs = new HashSet<String>();
    }



    /** Get all Blobs that are Staged for addition.
     * @return all blobs that are staged for addition. */
    public HashMap<String, String> getStagedForAddition() {
        return _stagedForAdditionBlobs;
    }

    /** Get all Blobs that are Staged for removal.
     * @return all blobs that are staged for removal. */
    public Set<String> getStagedForRemoval() {
        return _stagedRemovalBlobs;
    }

    /** Set the head of commit.
     * @param c Set the head of stage to Commit C*/
    public void setHead(Commit c) {
        _head = c;
    }

    /** Clear all Blobs. So that Status is clear after commit. */
    public void clearAllStages() {
        _stagedForAdditionBlobs.clear();
        _stagedRemovalBlobs.clear();
        _allBlobsToBeCommited.clear();
    }

    /** Check if all HashSets are cleared.
     * @return True if all staged Blobs are cleared. */
    public boolean isClear() {
        return _stagedForAdditionBlobs.isEmpty()
                && _stagedRemovalBlobs.isEmpty();
    }

    /** File named f get added in ADD COMMAND.
     * Description: Adds a copy of the file as it currently exists to
     * the staging area (see the description of the commit command).
     * For this reason, adding a file is also called staging the file
     * for addition. Staging an already-staged file overwrites the
     * previous entry in the staging area with the new contents.
     * The staging area should be somewhere in .gitlet. If the current
     * working version of the file is identical to the version in the
     * current commit, do not stage it to be added, and remove it from
     * the staging area if it is already there (as can happen when a
     * file is changed, added, and then changed back). The file will
     * no longer be staged for removal (see gitlet rm), if it was
     * at the time of the command.
     * @param fileName  file's name to add to Blob files for stage. */
    public void add(String fileName) {
        File file = new File(fileName);

        String blobSHA = sha1(readContents(file));
        String prevSHA = _head.getBlobSHA(fileName);

        if (prevSHA == null) {
            _stagedForAdditionBlobs.put(fileName, blobSHA);
        } else {
            if (!blobSHA.equals(prevSHA)) {
                _stagedForAdditionBlobs.put(fileName, blobSHA);
            } else {
                _stagedForAdditionBlobs.remove(fileName);
            }
        }

        if (_stagedRemovalBlobs.contains(fileName)) {
            _stagedRemovalBlobs.remove(fileName);
        }
    }

    /** COMMIT COMMAND.
     * Description: Saves a snapshot of certain files in the current commit and
     * staging area so they can be restored at a later time, creating a new
     * commit. The commit is said to be tracking the saved files. By default,
     * each commit's snapshot of files will be exactly the same as its parent
     * commit's snapshot of files; it will keep versions of files exactly as
     * they are, and not update them. A commit will only update the contents
     * of files it is tracking that have been staged for addition at the time
     * of commit, in which case the commit will now include the version of the
     * file that was staged instead of the version it got from its parent. A
     * commit will save and start tracking any files that were staged for
     * addition but weren't tracked by its parent. Finally, files tracked in
     * the current commit may be untracked in the new commit as a result being
     * staged for removal by the rm command (below).
     * @param msg The commit msg.
     * @param p1 the 1st parent (head of current branch) if exists
     * @param p2 the 2nd parent (head of the given branch) if exists
     * @return the Commit with the given msg param. */
    public Commit commit(String msg, Commit p1, Commit p2) {
        for (String fileName: _head.getBlobsPtr().keySet()) {
            _allBlobsToBeCommited.put(fileName, _head.getBlobSHA(fileName));
        }

        for (String keyBlobFileName: _stagedForAdditionBlobs.keySet()) {
            File f = new File(keyBlobFileName);
            String keyBlobSHA = sha1(readContents(f));
            _allBlobsToBeCommited.put(keyBlobFileName, keyBlobSHA);
            GitlitController.saveBlobToGitDir(f, keyBlobSHA);
        }

        for (String fileName: _stagedRemovalBlobs) {
            _allBlobsToBeCommited.remove(fileName);
        }

        Commit latestCommit = new Commit(msg, _head, _allBlobsToBeCommited);
        latestCommit.setMergedParent1(p1);
        latestCommit.setMergedParent2(p2);


        _head = latestCommit;
        clearAllStages();
        return latestCommit;
    }

    /** RM COMMAND.
     * CITE: A demo of how GIT RM works
     * https://stackoverflow.com/questions/2047465/how-can-i-delete-a
     * -file-from-a-git-repository
     * HashMap remove: returns values if key exists, else return null
     * @param fileName the blob to remove. */
    public void rm(String fileName) {
        boolean headHasBlob = _head.getBlobSHA(fileName) != null;
        String stagedFileSHA = _stagedForAdditionBlobs.remove(fileName);

        if (stagedFileSHA == null && !headHasBlob) {
            System.out.println("No reason to remove the file.");
        }

        if (headHasBlob) {
            _stagedRemovalBlobs.add(fileName);
            restrictedDelete(fileName);
        }
    }


    /** Extra Credit.
     * A file in the working directory is "modified but not staged" if it is
     *
     * 1) Tracked in the current commit, changed in the working directory,
     * but not staged; or
     * 2) Staged for addition, but with different contents than in the working
     * directory; or
     * 3) Staged for addition, but deleted in the working directory; or
     * 4) Not staged for removal, but tracked in the current commit and
     * deleted from the working directory.
     *
     * The final category ("Untracked Files") is for files
     * 1) present in the working directory but neither staged for addition nor
     * tracked.
     * This includes files that have been staged for removal, but then
     * re-created without Gitlet's knowledge. Ignore any subdirectories that
     * may have been introduced, since Gitlet does not deal with them.
     */
    public void statusModifiedAndUntracked() {
        List<String> filesInCWD = plainFilenamesIn(Main.CWD);
        List<String> modifiedBlobs = new LinkedList<>();
        List<String> untrackedBlobs = new LinkedList<>();
        boolean blobInCWD, blobInStagedAdd, blobInStagedRm, blobModified;

        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit c = _head;
        for (String blob: c.getBlobsPtr().keySet()) {
            blobInCWD = filesInCWD.contains(blob);
            blobInStagedAdd = _stagedForAdditionBlobs.containsKey(blob);
            blobInStagedRm = _stagedRemovalBlobs.contains(blob);
            if (blobInCWD) {
                blobModified = blobIsModified(blob, c.getBlobsPtr().get(blob));
            } else {
                blobModified = false;
            }

            if (blobInCWD && blobModified  && !blobInStagedAdd) {
                modifiedBlobs.add(blob + " (modified)");
            } else if (!blobInCWD && !blobInStagedRm) {
                modifiedBlobs.add(blob + " (deleted)");
            }
        }

        Collections.sort(modifiedBlobs);
        for (String blobName: modifiedBlobs) {
            System.out.println(blobName);
        }

        System.out.println("\n=== Untracked Files ===");
        boolean blobInCurrCommit;
        for (String blob: filesInCWD) {
            blobInStagedAdd = _stagedForAdditionBlobs.containsKey(blob);
            blobInStagedRm = _stagedRemovalBlobs.contains(blob);
            blobInCurrCommit = (c.getBlobSHA(blob) != null);

            if (!blobInStagedAdd && !blobInStagedRm && !blobInCurrCommit) {
                untrackedBlobs.add(blob);
            }
        }
        Collections.sort(untrackedBlobs);
        for (String blobName: untrackedBlobs) {
            System.out.println(blobName);
        }
        System.out.println();
    }

    /** Check if a file has been modified. USED in STATUS/MERGE.
     * @param fileName the name of file we want to check.
     * @param fileSHA the SHA code of the file.
     * We check in the CWD and in current staged file.
     * @return True if file has been modified in CWD. */
    public boolean blobIsModified(String fileName, String fileSHA) {
        File f = new File(fileName);
        String currSHA = sha1(readContents(f));
        return !currSHA.equals(fileSHA);
    }

    /** CHECKOUT COMMANDs.
     * @param fileName name of file in HEAD commit we want to checkout. */
    public void checkout(String fileName) {
        Commit c = GitlitController.getCommitInGitDir(_head.getCommitSHA());
        c.recover(fileName, c.getBlobSHA(fileName));
    }

    /** Check if the current _head commit is untracked.
     * Meaning there exists file(s) in the CWD where
     * .gitlet is located that have not been added to
     * be staged for addition.
     * @return true if the current commit is untracked. */
    public boolean commitIsUntracked() {
        HashMap<String, String> headBlobs = _head.getBlobsPtr();
        List<String> filesInCWD = plainFilenamesIn(Main.CWD);

        for (String blobName: filesInCWD) {
            if (_stagedForAdditionBlobs.get(blobName) == null
                    && headBlobs.get(blobName) == null) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }
}
