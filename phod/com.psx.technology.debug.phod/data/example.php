<?php
namespace Xlogic\Core\Controller;

class MainControllerTest extends \PHPUnit_Framework_TestCase {

    /**
     * The object to be tested, gets mocked btw.
     */
    protected $objut;
    
    public function setUp(){
        //FIXME constructor needs probably arguments - mock should override this
        // Limitations: final, private & static methods CANNOT be mocked.
        $calls=array(
            array('name'=>'processRoute',
            	'parameter'=>array('/Dmu/Overview/filter?PHPSESSID=jcjlqx8oshp1v4q1m05wolnb4lpsxi7y6ysa5495&DMU_OVERVIEW_FILTER[Msn][]=13'),
        		'return'=>'');
        $this->objut=$this->getMock('Xlogic\Core\Controller\MainController',array('processRoute'));
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