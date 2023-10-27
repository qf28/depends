package depends.extractor.python;

import depends.entity.CandidateTypes;
import depends.entity.FunctionEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.extractor.FileParser;
import depends.relations.Relation;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PythonParameterTypeDedudceTest extends PythonParserTest {
	@Before
	public void setUp() {
		super.init();
	}
	
	@Test
	public void test_deduce_type_of_parameter() throws IOException {
		String[] srcs = new String[] {
	    		"./src/test/resources/python-code-examples/deducetype_parameter.py",
	    	    };
	    
	    for (String src:srcs) {
		    FileParser parser = createFileParser();
		    parser.parse(src);
	    }
	    resolveAllBindings();
	    String name = withPackageName(srcs[0],"test");
	    FunctionEntity function = (FunctionEntity)( entityRepo.getEntity(name));
	    VarEntity var = function.lookupVarLocally("t1");
	    TypeEntity type = var.getType();
	    assertTrue(type instanceof CandidateTypes);
	    assertEquals(2,((CandidateTypes)type).getCandidateTypes().size());
	}
	
	
	@Test
	public void test_deduce_type_of_builtIn() throws IOException {
		String[] srcs = new String[] {
	    		"./src/test/resources/python-code-examples/deducetype_builtin.py",
	    	    };
	    
	    for (String src:srcs) {
		    FileParser parser = createFileParser();
		    parser.parse(src);
	    }
	    resolveAllBindings();
	    String name = withPackageName(srcs[0],"test");
	    FunctionEntity function = (FunctionEntity)( entityRepo.getEntity(name));
	    VarEntity var = function.lookupVarLocally("t1");
	    TypeEntity type = var.getType();
	    assertTrue(type == null);
	}

	
	@Test
	public void test_deduce_type_of_builtIn_cannot_override() throws IOException {
		String[] srcs = new String[] {
	    		"./src/test/resources/python-code-examples/deducetype_builtin.py",
	    	    };
	    
	    for (String src:srcs) {
		    FileParser parser = createFileParser();
		    parser.parse(src);
	    }
	    resolveAllBindings();
	    String name = withPackageName(srcs[0],"test2");
	    FunctionEntity function = (FunctionEntity)( entityRepo.getEntity(name));
	    VarEntity var = function.lookupVarLocally("t1");
	    TypeEntity type = var.getType();
	    assertTrue(type instanceof CandidateTypes);
	    assertEquals(1,((CandidateTypes)type).getCandidateTypes().size());
	}
	
	
	@Test
	public void test_deduce_type_of_non_param_var() throws IOException {
		String[] srcs = new String[] {
	    		"./src/test/resources/python-code-examples/deducetype_nonparam.py",
	    	    };
	    
	    for (String src:srcs) {
		    FileParser parser = createFileParser();
		    parser.parse(src);
	    }
	    resolveAllBindings();
	    String name = withPackageName(srcs[0],"test");
	    FunctionEntity function = (FunctionEntity)( entityRepo.getEntity(name));
	    VarEntity var = function.lookupVarLocally("t2");
	    TypeEntity type = var.getType();
	    assertTrue(type instanceof CandidateTypes);
	    assertEquals(1,((CandidateTypes)type).getCandidateTypes().size());
	}

	@Test
	public void test_expression_with_duck_type_should_introduce_possibile_relation() throws IOException {
		String[] srcs = new String[] {
				"./src/test/resources/python-code-examples/expression_reload_issue_test.py",
		};

		for (String src:srcs) {
			FileParser parser = createFileParser();
			parser.parse(src);
		}
		resolveAllBindings();
		String name = withPackageName(srcs[0],"test_expression");
		FunctionEntity function = (FunctionEntity)( entityRepo.getEntity(name));
		List<Boolean> result = function.getRelations().stream().map(Relation::possible).toList();
		List<Boolean> expected = Arrays.asList(false, false, true, true, true, false, false, true, true, false);
		assertArrayEquals(expected.toArray(),result.toArray());
	}


}

