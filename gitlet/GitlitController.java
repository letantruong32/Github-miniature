package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import static gitlet.Utils.*;


/**
 * @author Truong Le
 */
public class GitlitController implements Serializable {
    /** All commitsID (SHA). */
    private ArrayList<Commit> _commits;
    /** All branches Name (key) and its Commit. */
    private HashMap<String, Commit> _branches;
    /** Current Stage of gitlit. */
    private Stage _stage;
    /** Name of current branch. */
    private String _currBranch;
    /** SHA of current head commit. */
    private String _headSHA;

    /** Version Control System. */
    public GitlitController() {
        _commits = new ArrayList<>();
        _branches = new HashMap<String, Commit>();
        _stage = new Stage();
        _currBranch = "master";


        Commit initial = new Commit("initial commit", null,
                new HashMap<String, String>());
        _stage.setHead(initial);
        _commits.add(initial);
        saveCommitToGitDir(initial);
        _branches.put(_currBranch, initial);


        _headSHA = sha1(serialize(initial));
        saveBranchToGitDir(_headSHA);
    }

    /** Get commit C from COMMIT_DIR.
     * @param commitSHA the commit ID to get COMMIT: each SHA is unique.
     * @return the commit with the same SHA. */
    public static Commit getCommitInGitDir(String commitSHA) {
        File f = new File(Main.COMMIT_DIR.getPath() + "/" + commitSHA);
        return readObject(f, Commit.class);
    }
    /** Save commit to COMMIT_DIR.
     * @param c the commit to be saved. */
    public static void saveCommitToGitDir(Commit c) {
        String commitSHA =  sha1(serialize(c));
        File f = new File(Main.COMMIT_DIR.getPath() + "/" + commitSHA);
        writeObject(f, c);
    }

    /** Get blob contents from BLOB_DIR.
     * @param blobSHA the SHA Code of blob
     * @return the contents of blob file. */
    public static String getBlobInGitDir(String blobSHA) {
        File f = new File(Main.BLOBS_DIR + "/" + blobSHA);
        return readContentsAsString(f);
    }
    /** Save BLOBS to GITDIR.
     * @param f file in CWD.
     * @param blobSHA the SHA code of the file
     */
    public static void saveBlobToGitDir(File f, String blobSHA) {
        File blobFile = new File(Main.BLOBS_DIR.getPath() + "/" + blobSHA);
        writeContents(blobFile, readContents(f));
    }
    /** Write the content of given file into CWD.
     * @param fileName name of file to be written/overwritten.
     * @param blobSha the SHA1 Code of file
     */
    public static void writeBlobToCWD(String fileName, String blobSha) {
        File blobFile = new File(Main.BLOBS_DIR.getPath() + "/" + blobSha);
        writeContents(new File(fileName), readContents(blobFile));
    }

    /** Save branches to BRANCHES_DIR.
     * JUST FOR FUN.
     * @param branchSHA the SHA of branch to be saved. */
    public static void saveBranchToGitDir(String branchSHA) {
        File f = new File(Main.BRANCHES_DIR.getPath() + "/" + branchSHA);
        writeObject(f, branchSHA);
    }

    /** File named f get added to stage.
     * @param fileName add to the current to-be-committed. */
    public void addToStaged(String fileName) {
        File fileCWD = new File(fileName);
        if (!fileCWD.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else if (fileName.isEmpty()) {
            System.out.println("Filename is empty.");
            System.exit(0);
        } else if (fileCWD.isDirectory()) {
            System.out.println("File is a directory.");
            System.exit(0);
        } else {
            _stage.add(fileName);
        }
    }

    /** Make a COMMIT COMMAND.
     * @param msg the msg of the commit.
     * @param p1 parent1 of merge if exists, null otherwise
     * @param p2 paraent2 of merge if exits, null otherwise
     * Delegator Stage _stage class. */
    public void commit(String msg, Commit p1, Commit p2) {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        if (_stage.isClear()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit c = _stage.commit(msg, p1, p2);
        _commits.add(c);
        saveCommitToGitDir(c);
        _headSHA = sha1((serialize(c)));
        _branches.put(_currBranch, c);
    }

    /** Make a RM COMMAND.
     * @param fileName name of blob to be removed.
     * Delegate code in STAGE _stage */
    public void rm(String fileName) {
        File fileCWD = new File(fileName);
        if (fileName.isEmpty()) {
            System.out.println("Filename is empty.");
            System.exit(0);
        } else if (fileCWD.isDirectory()) {
            System.out.println("File is a directory.");
            System.exit(0);
        } else {
            _stage.rm(fileName);
        }
    }

    /** Print log head. */
    public void log() {
        String currSHA = _headSHA;
        while (currSHA != null) {
            Commit c = getCommitInGitDir(currSHA);
            c.log();
            if (c.getParent() != null) {
                currSHA = c.getParent().getCommitSHA();
            } else {
                break;
            }
        }
    }

    /** Print all commits ever made. */
    public void globalLog() {
        for (int i = 0; i < _commits.size(); i++) {
            _commits.get(i).log();
        }
    }

    /** Find commits with given msg.
     * @param msg the msg of the commit. */
    public void find(String msg) {
        int foundMessage = 0;
        for (Commit c: _commits) {
            if (c.getMessage().equals(msg)) {
                System.out.println(c.getCommitSHA());
                foundMessage += 1;
            }
        }
        if (foundMessage == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** The STATUS COMMAND. */
    public void status() {
        status("Branches");
        status("Addition");
        status("Removal");
        _stage.statusModifiedAndUntracked();
    }

    /** STATUS COMMAND.
     * @param name the Name of status want to check.
    /** Check all file in staged for addition. */
    public void status(String name) {
        String kind = null;
        ArrayList<String> sortedByName = null;
        if (name.equals("Branches")) {
            kind = "=== Branches ===";
            sortedByName =
                    new ArrayList<String>(_branches.keySet());
        } else if (name.equals("Addition")) {
            kind = "=== Staged Files ===";
            sortedByName = new ArrayList<String>(
                    _stage.getStagedForAddition().keySet());
        } else if (name.equals("Removal")) {
            kind = "=== Removed Files ===";
            sortedByName =
                    new ArrayList<String>(_stage.getStagedForRemoval());
        } else {
            System.exit(0);
        }
        Collections.sort(sortedByName);

        System.out.println(kind);
        for (String blobName: sortedByName) {
            if (name.equals("Branches")) {
                if (blobName.equals(_currBranch)) {
                    System.out.println("*" + blobName);
                } else {
                    System.out.println(blobName);
                }
            } else {
                System.out.println(blobName);
            }
        }
        System.out.println();
    }

    /** CHECKOUT COMMANDs.
     * @param fileName name of file in HEAD commit we want to checkout. */
    public void checkout(String fileName) {
        File fileCWD = new File(fileName);
        if (fileName.isEmpty()) {
            System.out.println("File is empty.");
            System.exit(0);
        } else if (fileCWD.isDirectory()) {
            System.out.println("File is a directory.");
        } else {
            _stage.checkout(fileName);
        }
    }

    /** Check out a filename (history) with given commit ID.
     * @param commitID the SHA code of given commit ID.
     * @param fileName the name of file we want to checkout. */
    public void checkout(String commitID, String fileName) {
        int foundCommit = 0;
        String commitSHA = "";
        for (Commit c : _commits) {
            if (c.getCommitSHA().substring(0, commitID.length()).
                    equals(commitID)) {
                foundCommit += 1;
                commitSHA = c.getCommitSHA();
                break;
            }
        }
        if (foundCommit == 0) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (foundCommit > 1) {
            System.out.print("SHA-1 is broken.");
        } else {
            Commit thisCommit = getCommitInGitDir(commitSHA);
            String blobSHA = thisCommit.getBlobSHA(fileName);
            thisCommit.recover(fileName, blobSHA);
        }
    }

    /** Check out (go back to) BRANCH.
     * @param branchName name of the branch want to revert to. */
    public void checkoutBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (_currBranch.equals(branchName)) {
            System.out.println("No need to check out the current branch.");
            System.exit(0);
        } else {
            if (_stage.commitIsUntracked()) {
                System.exit(0);
            }
            List<String> filesInCWD = plainFilenamesIn(Main.CWD);
            Commit branchCommit = getCommitInGitDir(
                    _branches.get(branchName).getCommitSHA());
            ArrayList<String> branchBlobs =
                    new ArrayList<>(branchCommit.getBlobsPtr().keySet());

            for (String blobName : branchCommit.getBlobsPtr().keySet()) {
                String blobSHA = branchCommit.getBlobSHA(blobName);
                branchCommit.recover(blobName, blobSHA);
            }

            for (String fileCWD: filesInCWD) {
                if (!branchBlobs.contains(fileCWD)) {
                    restrictedDelete(fileCWD);
                }
            }
            _stage.setHead(branchCommit);
            _stage.clearAllStages();
            _currBranch = branchName;
            _headSHA = _branches.get(branchName).getCommitSHA();
        }
    }

    /** Creates a new Branch with given BranchName.
     * @param branchName create a new branch with this name. */
    public void branch(String branchName) {
        if (_branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists");
            System.exit(0);
        }

        saveBranchToGitDir(_headSHA);
        Commit c = getCommitInGitDir(_headSHA);
        _branches.put(branchName, c);
    }

    /** Remove the branch given branchName.
     * @param branchName remove branch with this name. */
    public void rmBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (_currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {
            _branches.remove(branchName);
        }
    }

    /** Reset Commit Command. Similar to branch checkout.
     * Checks out all the files tracked by the given commit.
     * @param commitSHA reset Commit with the SHA commitID. */
    public void reset(String commitSHA) {
        int found = 0;
        Commit wantedCommit = null;
        for (Commit c: _commits) {
            if (c.getCommitSHA().equals(commitSHA)) {
                found += 1;
                wantedCommit = c;
                break;
            }
        }
        if (found == 0) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (found > 1) {
            System.out.println("Hash SHA-1 is broken.");
        } else if (wantedCommit != null) {
            if (_stage.commitIsUntracked()) {
                System.exit(0);
            }

            List<String> filesInCWD = plainFilenamesIn(Main.CWD);
            HashMap<String, String> wantedBlobs = wantedCommit.getBlobsPtr();

            for (String wantedBlob: wantedBlobs.keySet()) {
                String blobSHA = wantedBlobs.get(wantedBlob);
                wantedCommit.recover(wantedBlob, blobSHA);
            }

            for (String fileCWD: filesInCWD) {
                if (!wantedBlobs.containsKey(fileCWD)) {
                    restrictedDelete(fileCWD);
                }
            }
            _stage.clearAllStages();
            _stage.setHead(wantedCommit);
            _branches.put(_currBranch, getCommitInGitDir(commitSHA));
            _headSHA = commitSHA;
        } else {
            System.exit(0);
        }
    }


    /** MERGE COMMAND.
     * Careful: mergeError and Split changes pointers of current
     * and given Commits.
     * Inspiration: https://git-scm.com/book/en/v2/Git-Branching-Basic-
     * Branching-and-Merging (Fast-forward, 3-way merge and conflicts)
     * @param branchName the branch to merge from. */
    public void merge(String branchName) {
        if (mergeErros(branchName)) {
            System.exit(0);
        }
        Commit current = getCommitInGitDir(_headSHA);
        Commit given = getCommitInGitDir(
                _branches.get(branchName).getCommitSHA());
        Commit splitPt = splitPoint(current, given);

        HashMap<String, String> currBlobs = current.getBlobsPtr();
        HashMap<String, String> givenBlobs = given.getBlobsPtr();
        HashMap<String, String> splitBlobs = splitPt.getBlobsPtr();

        int mergeConflict = mergeHelper(currBlobs, givenBlobs, splitBlobs);

        Commit p1 = _branches.get(branchName);
        Commit p2 = getCommitInGitDir(_headSHA);
        commit("Merged " + branchName + " into " + _currBranch
                + ".", p1, p2);

        if (mergeConflict == 1) {
            System.out.println("Encountered a merge conflict.");
        }


    }

    /** Merge Helper Delegation.
     * @param currBlobs Blobs of current head commit.
     * @param givenBlobs Blobs of given branch commit.
     * @param splitBlobs Blobs of split point commit.
     * @return True if there is a merge conflict.
     * Written by the Order of the spec, including 8 bullet points. */
    public int mergeHelper(HashMap<String, String> currBlobs,
                            HashMap<String, String> givenBlobs,
                            HashMap<String, String> splitBlobs) {
        int mergeConflict = 0;
        String currBlobSHA, givenBlobSHA, splitBlobSHA;

        for (String blobFile: givenBlobs.keySet()) {
            currBlobSHA = currBlobs.get(blobFile);
            givenBlobSHA = givenBlobs.get(blobFile);
            splitBlobSHA = splitBlobs.get(blobFile);

            cond1and5(currBlobSHA, givenBlobSHA, splitBlobSHA, givenBlobs);
            cond2(currBlobSHA, givenBlobSHA, splitBlobSHA);
            cond3(currBlobSHA, givenBlobSHA, splitBlobSHA, givenBlobs);
            cond7(currBlobSHA, givenBlobSHA, splitBlobSHA);
        }

        for (String blobFile: currBlobs.keySet()) {
            currBlobSHA = currBlobs.get(blobFile);
            givenBlobSHA = givenBlobs.get(blobFile);
            splitBlobSHA = splitBlobs.get(blobFile);

            cond4(currBlobSHA, givenBlobSHA, splitBlobSHA);
            cond6(currBlobSHA, givenBlobSHA, splitBlobSHA, splitBlobs);
            if (cond8(blobFile, currBlobSHA, givenBlobSHA, splitBlobSHA)) {
                mergeConflict = 1;
            }
        }
        return mergeConflict;
    }

    /** Condition#1: Any files that have been modified in the given
     * branch since the split point, but not modified in the current
     * branch since the split point should be changed to their versions
     * in the given branch (checked out from the commit at the front
     * of the given branch). These files should then all be automatically
     * staged. To clarify, if a file is "modified in the given branch
     * since the split point" this means the version of the file as it
     * exists in the commit at the front of the given branch has different
     * content from the version of the file at the split point.
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     * @param givenBlobs all Blobs in the merge given branch
     */
    /** Condition#5: Any files that were not present at the split point and
     *  are present only in the given branch should be checked out and staged.
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     * @param givenBlobs all Blobs in the merge given branch
     */
    public void cond1and5(String currSHA, String givenSHA,
                      String splitSHA, HashMap<String, String> givenBlobs) {
        if ((currSHA == null && splitSHA == null)
                || (splitSHA != null && !splitSHA.equals(givenSHA)
                    && splitSHA.equals(currSHA))) {
            String desiredFileName = null;
            for (String givenFileName : givenBlobs.keySet()) {
                if (givenBlobs.get(givenFileName).equals(givenSHA)) {
                    desiredFileName = givenFileName;
                    break;
                }
            }
            writeBlobToCWD(desiredFileName, givenSHA);
            addToStaged(desiredFileName);
        }
    }

    /** Condition#2: Any files that have been modified in the current
     * branch but not in the given branch since the split point should
     * stay as they are.
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     */
    public void cond2(String currSHA, String givenSHA,
                      String splitSHA) {
        if (splitSHA != null && currSHA != null) {
            if (!splitSHA.equals(currSHA) && splitSHA.equals(givenSHA)) {
                return;
            }
        }
    }

    /** Condition#3: Any files that have been modified in both the current
     *  and given branch in the same way (i.e., both to files with the same
     *  content or both removed) are left unchanged by the merge. If a file
     *  is removed in both, but a file of that name is present in the working
     *  directory that file is not removed from the working directory (but it
     *  continues to be absent—not staged—in the merge).
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     * @param givenBlobs all Blobs in the merge given branch
     */
    public void cond3(String currSHA, String givenSHA,
                      String splitSHA, HashMap<String, String> givenBlobs) {
        if (splitSHA != null && currSHA != null) {
            if (givenSHA.equals(currSHA) && !currSHA.equals(splitSHA)) {
                return;
            }
        }
    }

    /** Condition#4: Any files that were not present at the split point and
     *  are present only in the current branch should remain as they are.
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     */
    public void cond4(String currSHA, String givenSHA,
                      String splitSHA) {
        if (splitSHA == null && currSHA != null && givenSHA == null) {
            return;
        }
    }

    /**  Condition#6: Any files present at the split point, unmodified in
     *  the current branch, and absent in the given branch should be removed
     *  (and untracked).
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     * @param splitBlobs the blobs in split commit
     */
    public void cond6(String currSHA, String givenSHA,
                      String splitSHA, HashMap<String, String> splitBlobs) {
        if (splitSHA != null && givenSHA == null && currSHA.equals(splitSHA)) {
            String desiredFileName = null;
            for (String splitFileName : splitBlobs.keySet()) {
                if (splitBlobs.get(splitFileName).equals(splitSHA)) {
                    desiredFileName = splitFileName;
                    break;
                }
            }
            rm(desiredFileName);
        }
    }

    /**  Condition#7: Any files present at the split point, unmodified in
     * the given branch, and absent in the current branch should remain absent.
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     */
    public void cond7(String currSHA, String givenSHA, String splitSHA) {
        if (givenSHA.equals(splitSHA) && currSHA == null) {
            return;
        }
    }

    /**  Condition#8: Any files modified in different ways in the current and
     * given branches are in conflict. "Modified in different ways" can mean
     * that the contents of both are changed and different from other, or the
     * contents of one are changed and the other file is deleted, or the file
     * was absent at the split point and has different contents in the given
     * and current branches. In this case, replace the contents of the
     * conflicted file with
     <<<<<<< HEAD
     contents of file in current branch
     =======
     contents of file in given branch
     >>>>>>>
     (replacing "contents of..." with the indicated file's contents) and stage
     the result. Treat a deleted file in a branch as an empty file. Use straight
     concatenation here. In the case of a file with no newline at the end, you
     might well end up with something like this:
     <<<<<<< HEAD
     contents of file in current branch=======
     contents of file in given branch>>>>>>>
     * @param fileName the name of file to overwrite conflict content
     * @param currSHA the SHA of current (master branch)
     * @param givenSHA the SHA of given branch
     * @param splitSHA the SHA of split point
     * #diff1: The contents of both are changed and different from other
     * #diff2: The contents of one are changed and the other file is deleted
     * #diff3: The file was absent at the split point and has different contents
     *        in the given and current branches.
     * @return true if satisfies cond8
     */
    public boolean cond8(String fileName, String currSHA,
                         String givenSHA, String splitSHA) {
        boolean diff1 = givenSHA != null && splitSHA != null
                && !givenSHA.equals(currSHA) && !givenSHA.equals(splitSHA)
                && !currSHA.equals(splitSHA);

        boolean diff2 = (splitSHA != null && !splitSHA.equals(currSHA)
                && givenSHA == null);

        boolean diff3 = (splitSHA == null && givenSHA != null
                && !currSHA.equals(givenSHA));

        if (diff1 || diff2 || diff3) {
            writeMergeConflicts(fileName, givenSHA, currSHA);
            addToStaged(fileName);
            return true;
        }
        return false;
    }

    /** Write Conflict into CWD.
     * @param fileName name of file to write to in CWD.
     * @param givSHA name of SHA from given Branch.
     * @param currSHA name of SHA from current Commit*/
    public void writeMergeConflicts(String fileName,
                                    String givSHA, String currSHA) {

        String curContents = currSHA != null ? getBlobInGitDir(currSHA) : null;
        String givContents = givSHA != null ? getBlobInGitDir(givSHA) : null;
        String newCWDContents = null;

        if (curContents != null && givContents != null) {
            newCWDContents = "<<<<<<< HEAD\n" + curContents
                    + "=======\n" + givContents + ">>>>>>>\n";
        } else if (curContents == null) {
            newCWDContents = "<<<<<<< HEAD\n" + "=======\n"
                    + givContents + ">>>>>>>\n";
        } else if (givContents == null) {
            newCWDContents = "<<<<<<< HEAD\n" + curContents
                    + "=======\n" + ">>>>>>>\n";
        }
        writeContents(new File(fileName), newCWDContents);
    }

    /** Get the SPLITPOINT COMMIT.
     * @param current usually the master branch
     * @param given desired merge branch
     * CITE: https://stackoverflow.com/questions/25563797/git-branch-split-point
     * Dangerous: after split, current and given point to parents
     *             (not themselves)
     * @return the split point commit*/
    public Commit splitPoint(Commit current, Commit given) {
        Commit pCurr = current;
        Commit pGiv = given;
        while (current.getParent() != null) {
            if (current.getMergedParent1() != null) {
                Commit p1 = current.getMergedParent1();
                Commit givenP = given.getParent();
                while (givenP != null) {
                    if (givenP.equals(p1)) {
                        return givenP;
                    }
                    givenP = givenP.getParent();
                }
            }
            current = current.getParent();
        }

        current = pCurr; given = pGiv;
        int currToInit = current.distanceToInit();
        int givenToInit = given.distanceToInit();
        int diffDist = givenToInit - currToInit;
        if (diffDist > 0) {
            while (diffDist > 0) {
                given = given.getParent();
                diffDist--;
            }
        } else {
            diffDist = -diffDist;
            while (diffDist > 0) {
                current = current.getParent();
                diffDist--;
            }
        }
        while (!given.equals(current)) {
            given = given.getParent();
            current = current.getParent();
        }
        return current;
    }

    /** All merge errors/conflicts.
     * @param branchName the branch want to merge
     * @return true if there are errors.
     * Branch and Merge Ideas:
     * https://www.youtube.com/watch?v=FyAAIHHClqI
     * */
    public boolean mergeErros(String branchName) {
        if (_stage.commitIsUntracked()) {
            return true;
        }
        if (!_stage.isClear()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!_branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (_currBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }

        Commit current = getCommitInGitDir(_headSHA);
        Commit given = getCommitInGitDir(
                _branches.get(branchName).getCommitSHA());
        Commit splitPt = splitPoint(current, given);

        if (given.equals(splitPt)) {
            System.out.println("Given branch is an ancestor of"
                    + " the current branch.");
            return true;
        }
        if (current.equals(splitPt)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }

        return false;
    }


    /** TESTING METHOD: FUN "FAR" COMMAND LINE.
     * @param k th Kth commit from commitID.
     * @param commitID from this commit the disct.
     * @return Kth commit from commitID*/
    public Commit far(String commitID, String k) {
        boolean trueee = false;
        Commit cc = null;
        for (Commit c: _commits) {
            if (c.getCommitSHA().equals(commitID)) {
                trueee = true;
                cc = c;
                break;
            }
        }
        System.out.println(trueee);
        System.out.println(cc.far(Integer.parseInt(k)).getMessage());
        return cc.far(Integer.parseInt(k));
    }
}
