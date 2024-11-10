package develop.x.core.dispatcher;

public sealed interface XDispatcher permits AbstractXDispatcher{
    void invoke(Object item);
}
