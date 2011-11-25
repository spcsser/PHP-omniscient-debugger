<!-- BEGIN: main -->
<?php
namespace <$Namespace$>;

class <$Classname$>Test extends \PHPUnit_Framework_TestCase {

    /**
     * The object to be tested, gets mocked btw.
     */
    protected $objut;
    
    public function setUp(){
        //FIXME constructor needs probably arguments - mock should override this
        $this->objut=$this->getMock('<$Classname$>','<$Array4Methods2Stub$>');
        foreach() {
            
        }
    }
    
    public function tearDown(){
        
    }
    
    /**
     * @test
     */
    public function <$Methodname$>() {
        
    }
}
<!-- END: main -->