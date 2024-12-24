package ch.puzzle.itc.mobiliar.business.function.boundary;

public interface AddFunctionUseCase {
    Integer add(AddFunctionCommand addFunctionCommand) throws IllegalStateException;
}
