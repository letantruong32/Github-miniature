package gitlet;

import java.io.File;
import static gitlet.Utils.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Truong Le
 */
public class Main {
    /** Current Working Directory. */
    static final File CWD = new File(".");
    /** Gitlet folder. */
    static final File GITLIT_DIR = join(CWD, ".gitlet");
    /** Blobs folder, inside of .gitlet. */
    static final File BLOBS_DIR = join(GITLIT_DIR, "blobs");
    /** Commits folder, inside of .gitlet. */
    static final File COMMIT_DIR = join(GITLIT_DIR, "commits");
    /** Branches folder, inside of .gitlet. */
    static final File BRANCHES_DIR = join(GITLIT_DIR, "branches");
    /** Version of GitlitController, inside of .gitlet. */
    static final File VERSION_DIR = join(GITLIT_DIR, "gitlet");
    /** The main Lines of Gitlit.
     * @param args COMMAND LINEs. */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (!GITLIT_DIR.exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        GitlitController gitlit = loadGitLit();

        switch (args[0]) {
        case "init":        validateNumArgs("init", args, 1);
            gitlit = initCommand(); break;
        case "add":         validateNumArgs("add", args, 2);
            gitlit.addToStaged(args[1]); break;
        case "commit":      validateNumArgs("commit", args, 2);
            commitCommand(gitlit, args); break;
        case "rm":          validateNumArgs("rm", args, 2);
            gitlit.rm(args[1]); break;
        case "log":         validateNumArgs("log", args, 1);
            gitlit.log(); break;
        case "global-log":  validateNumArgs("global-log", args, 1);
            gitlit.globalLog(); break;
        case "find":        validateNumArgs("find", args, 2);
            gitlit.find(args[1]); break;
        case "status":      validateNumArgs("status", args, 1);
            gitlit.status(); break;
        case "checkout":
            checkoutCommand(gitlit, args); break;
        case "branch":      validateNumArgs("branch", args, 2);
            gitlit.branch(args[1]); break;
        case "rm-branch":   validateNumArgs("rm-branch", args, 2);
            gitlit.rmBranch(args[1]); break;
        case "reset":       validateNumArgs("reset", args, 2);
            gitlit.reset(args[1]); break;
        case "merge":       validateNumArgs("merge", args, 2);
            gitlit.merge(args[1]); break;
        case "add-remote":  validateNumArgs("add-remove", args, 3);
        break;
        case "rm-remote":   validateNumArgs("rm-remove", args, 2);
        break;
        case "push":        validateNumArgs("push", args, 3);
        break;
        case "fetch":       validateNumArgs("fetch", args, 3);
        break;
        case "pull": validateNumArgs("pull", args, 3);
        break;
        default:
            System.out.println("No command with that name exists");
            break;
        }

        File gitlitFile = new File(VERSION_DIR.getPath());
        writeObject(gitlitFile, gitlit);
    }

    /** Extra Credit REMOTE.
     * @param gitlit current controller version
     * @param args command line args*/
    public static void remoteCommand(GitlitController gitlit, String... args) {
        switch (args[0]) {

        default: break;
        }
    }

    /** Too long to put in main, so do it here.
     * @return a new Gitlit Controller.*/
    public static GitlitController initCommand() {
        if (GITLIT_DIR.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory.");
            System.exit(0);
        }
        GITLIT_DIR.mkdir();
        BLOBS_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BRANCHES_DIR.mkdir();

        GitlitController gitlit = new GitlitController();
        File gitlitFile = new File(VERSION_DIR.getPath());
        writeObject(gitlitFile, gitlit);

        return gitlit;
    }

    /** Too long to put in main, so do it here.
     * @param g current GitlitController.
     * @param args command line. */
    public static void commitCommand(GitlitController g, String... args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message");
            System.exit(0);

        } else if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        g.commit(args[1], null, null);
    }

    /** Too long to put in main, so do it here.
     * @param g current GitlitController.
     * @param args command line. */
    public static void checkoutCommand(GitlitController g, String... args) {
        if (args.length == 2) {
            g.checkoutBranch(args[1]);
        } else if (args.length == 3 && (args[1].equals("--"))) {
            g.checkout(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            g.checkout(args[1], args[3]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Load gitlit if it's already inside the CWD.
     * @return the Gitlit Controller if exists. */
    public static GitlitController loadGitLit() {
        GitlitController controller = null;
        File gitlit = new File(VERSION_DIR.getPath());
        if (gitlit.exists()) {
            controller = readObject(gitlit, GitlitController.class);
        }
        return controller;
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     * CITE: From lab12.
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected argument
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}

