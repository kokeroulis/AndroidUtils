package kokeroulis.gr.uiforms.validators;

public class ValidatorDelegate<T extends Comparable> {

    private T mValue;
    private Validator mValidatorInterface;

    private ValidatorDelegate(Validator validatorInterface) {
        mValidatorInterface = validatorInterface;
    }


    public static <T extends Comparable> ValidatorDelegate create(Validator<T> validatorInterface) {
        return new ValidatorDelegate(validatorInterface);
    }

    public T getValue() {
        return mValue;
    }

    public void setValue(T value) {
        mValue = value;
    }
}
