import org.grails.plugin.resource.mapper.MapperPhase
import org.grails.plugin.resource.ResourceMeta
import org.lesscss.LessCompiler
import spock.lang.*
import grails.plugin.spock.UnitSpec
/**
 * @author Gary Turovsky
 *
 */

class LesscssCompilationResourceMapperSpec extends UnitSpec {

    
    LessCompiler lessCompiler

    def config = [:]
    String lessFilePath = "web-app/less/test.less"
    String workDirPath = "web-app/less/"

    File workDir = new File(workDirPath)
    File input = new File(lessFilePath)

    String cssFilePath = input.absolutePath.replaceAll(/(?i)\.less/, '_less.css')
    File target = new File(cssFilePath)

    ResourceMeta resource 
    void setup() {
        resource = new ResourceMeta(contentType:'',  tagAttributes:[rel:'stylesheet/less'])
        resource.processedFile = input
        resource.sourceUrl = lessFilePath
        resource.workDir = workDir
    }

    void cleanup() {
       if(target.exists())
            target.delete() 
    }

    def "compiling works"(){
        
        when:
            LessCompiler compilerTest = new LessCompiler()
            compilerTest.compile(input, target)
        then:
            input.exists()
            target.exists()
    }

    def "mapper phase is GENERATION"() {
        when:
            LesscssCompilationResourceMapper mapper = new LesscssCompilationResourceMapper()
        then:
            mapper.phase == MapperPhase.GENERATION
    }

    def "mapper generates valid CSS file paths from LESS file paths"() {
        LesscssCompilationResourceMapper mapper = new LesscssCompilationResourceMapper()

        expect:
            mapper.generateCompiledFileFromOriginal(lessFilePath) == cssFilePath
        where:
            lessFilePath << [
                'foo/bar.less',
                'foo/bar.LESS',
                'foo/./bar.less',
                'foo/less/bar.less'
            ]

            cssFilePath << [
                'foo/bar_less.css',
                'foo/bar_less.css',
                'foo/./bar_less.css',
                'foo/less/bar_less.css'
                ]
   }

    def "mapper calls compiler"() {
        def compiler = Mock(LessCompiler)
        LesscssCompilationResourceMapper mapper = new LesscssCompilationResourceMapper(compiler)
        
        when:
            mapper.map(resource,config)
        then:
            1*compiler.compile(input,_)
    }

    def "mapper updates resource to new file location"() {
        ResourceMeta mockResource = Mock()
        mockResource.processedFile >> input
        LesscssCompilationResourceMapper mapper = new LesscssCompilationResourceMapper()
        
        when:
            mapper.map(mockResource,config)
        then:
            1*mockResource.updateActualUrlFromProcessedFile()
    }

    def "mapper creates valid resource"() {
        LesscssCompilationResourceMapper mapper = new LesscssCompilationResourceMapper()

        when:
            mapper.map(resource,config)
        then:
            resource.tagAttributes.rel == 'stylesheet'
            resource.contentType == 'text/css'
            resource.processedFile.path == target.path
            input.exists()
            target.exists()
    }
}
