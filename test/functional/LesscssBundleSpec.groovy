import geb.spock.GebSpec
/**
 * @author Paul Fairless
 * @author Gary Turovsky
 *
 */

import org.apache.commons.io.FileUtils;

class LesscssBundleSpec extends GebSpec {
    def setupSpec() {
//        browser.getDriver().setJavascriptEnabled(true)
    }
    def "check lesscss rules rendered"(){
        when:
        go('http://localhost:8080/lesscss-resources')

        then:
        $('h1').text() == 'Less Test'
        $('h1').jquery.css('color') == 'rgb(34, 34, 251)'
        $('h2').jquery.css('color') == 'rgb(132, 34, 16)'
        $('h3').jquery.css('color') == 'rgb(34, 251, 34)'

        and:'css processor still runs'
        $('h1').jquery.css('background-image') =~ '.*/static/images/header-pattern.png.*'
    }

    def "check on-the-fly compilation"(){
        File lessFile = new File("web-app/less/test.less")
        File oldLessFile = new File("web-app/less/test.less.old")
        File varLessFile = new File("web-app/less/test_variant.less")

        def oldColor, newColor

        when:
            
            go('http://localhost:8080/lesscss-resources')
            oldColor = $('h1').jquery.css('color')
            FileUtils.copyFile(lessFile, oldLessFile);

            FileUtils.copyFile(varLessFile, lessFile);
            
            go('http://localhost:8080/lesscss-resources')
            newColor = $('h1').jquery.css('color')
            FileUtils.copyFile(oldLessFile, lessFile);

        then:
            oldColor != newColor 
    }
}