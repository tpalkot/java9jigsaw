module com.implementer {
    requires com.service;
    exports com.implementer;
    provides com.service.MyService with com.implementer.MyServiceImplementer;
}