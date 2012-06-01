import org.grails.plugin.resource.mapper.MapperPhase

/**
 * @author Paul Fairless, Gary Turovsky
 *
 * Mapping file to compile .less files into .css files
 */
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.lesscss.LessCompiler
import org.lesscss.LessException

class LesscssCompilationResourceMapper implements GrailsApplicationAware {

    GrailsApplication grailsApplication

    def phase = MapperPhase.GENERATION 
    def operation = "compilation"

    static defaultIncludes = ['**/*.less']

    def map(resource, config) {
        //TODO: Instantiating the compiler here slows LESS compilation down, 
        //but moving it to class scope causes occasional errors.
        LessCompiler lessCompiler = new LessCompiler()
        
        File input = resource.processedFile
        File target = new File(generateCompiledFileFromOriginal(input.absolutePath))
        
        if (log.debugEnabled) {
            log.debug "Compiling LESS file [${input}] into [${target}]"
        }

        try {
            //Compile LESS file
            lessCompiler.compile input, target

            // Update mapping entry
            // This is now a CSS file, not a LESS file
            resource.processedFile = target

            // TODO: Verify that this is necessary
            resource.sourceUrlExtension = 'css'
            resource.contentType = 'text/css'
            resource.tagAttributes?.rel = 'stylesheet'

            resource.updateActualUrlFromProcessedFile()

        } catch (LessException e) {
            log.error("error compiling less file: ${input}", e)
        }

    }

    private String generateCompiledFileFromOriginal(String original) {
         original.replaceAll(/(?i)\.less/, '_less.css')
    }
}
