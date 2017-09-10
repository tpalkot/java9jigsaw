module com.consumer {
    exports com.consumer;
    requires java.logging;
    requires com.service;
    uses com.service.MyService;
    requires com.implementer;
}
