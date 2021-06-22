package gitlet;

/** General exception indicating a Gitlet error.  For fatal errors, the
 *  result of .getMessage() is the error message to be printed.
 *  @author P. N. Hilfinger
 */
class GitletException extends RuntimeException {


    /** A GitletException with no message. */
    GitletException() {
        super();
    }

    /** A GitletException MSG as its message. */
    GitletException(String msg) {
        super(msg);
    }


    /** A utility method that returns a new exception with a message
     *  formed from MSGFORMAT and ARGUMENTS, interpreted as for the
     *  String.format method or the standard printf methods.
     *
     *  The use is thus 'throw error(...)', which tells the compiler that
     *  execution will terminate at that point, and avoid insistance on
     *  an explicit return in a value-returning function.)  */
    static GitletException error(String msgFormat, Object... arguments) {
        return new GitletException(String.format(msgFormat, arguments));
    }

}
