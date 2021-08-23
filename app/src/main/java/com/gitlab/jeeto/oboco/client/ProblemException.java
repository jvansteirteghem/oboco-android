package com.gitlab.jeeto.oboco.client;

public class ProblemException extends Exception {
    private static final long serialVersionUID = 1L;
    private ProblemDto problem;

    public ProblemException(ProblemDto problem) {
        super(problem.getStatusCode() + ": " + problem.getCode() + " - " + problem.getDescription());

        this.problem = problem;
    }

    public ProblemException(ProblemDto problem, Throwable cause) {
        super(problem.getStatusCode() + ": " + problem.getCode() + " - " + problem.getDescription(), cause);

        this.problem = problem;
    }

    public ProblemDto getProblem() {
        return problem;
    }
}
