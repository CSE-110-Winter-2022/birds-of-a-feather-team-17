package edu.ucsd.cse110.bof;
public class Contract {
    public static boolean enabled = true;

    public static void REQUIRE(boolean expression, String property) throws ViolationException {
        if (enabled && !expression) throw new ViolationException(
                "Precondition violated");
    }

    public static void ENSURE(boolean expression, String property) throws ViolationException {
        if (enabled && !expression) throw new ViolationException(
                "Postcondition violated");
    }

    public static void INVARIANT(boolean expression, String property) throws ViolationException {
        if (enabled && !expression) throw new ViolationException(
                "Invariant violated");
    }

    public static class ViolationException extends RuntimeException {
        ViolationException(String message) { super(message); }
    }
}
