import org.grails.plugin.resource.mapper.MapperPhase

/**
 * @author Paul Fairless
 * @author Gary Turovsky
 *
 * Mapping file to compile .less files into .css files
 */
import org.lesscss.LessCompiler
import org.lesscss.LessException
import org.lesscss.LessSource


class LesscssCompilationResourceMapper {

    def phase = MapperPhase.GENERATION 
    def operation = "compilation"

    static defaultIncludes = ['**/*.less']

    def testingCompiler

    public LesscssCompilationResourceMapper() {
    }

    public LesscssCompilationResourceMapper(def testCompiler) {
        testingCompiler = testCompiler
    }

    def map(resource, config) {

        def lessCompiler

        if(testingCompiler == null)
            lessCompiler = new LessCompiler()
        else
            lessCompiler = testingCompiler

        File input = resource.processedFile
        File target = new File(generateCompiledFileFromOriginal(input.absolutePath))
        
        if (log.debugEnabled) {
            log.debug "Compiling LESS file [${input}] into [${target}]"
        }

        if(input && input.exists()) {
            try {

                // Compile LESS file
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
                log.error("Error compiling less file: ${input}", e)
            } catch(FileNotFoundException e) {
                log.error("""\
Error compiling $input.
You may be missing an imported LESS file in your resource bundle.  Make sure to bundle all imports and imports of imports.
LESS compiler error: $e
""")
            }

        }
    }

    private String generateCompiledFileFromOriginal(String original) {
         original.replaceAll(/(?i)\.less/, '_less.css')
    }
}
