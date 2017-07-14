package gr.patouchas.builder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;


public class GenerateBuilderTest {

	final List<Class> classes = Arrays.asList(User.class, Category .class);

	// the file will be saved under the projectJavaCode/src/main/java/package/name/ - if package contains the word
	// domain, it will be replace by builder ie:
	// pakage name: eu.iri.lmx.core.model.domain.configuration.descriptives
	// location to save:
	// D:\source\project\src\main\java\model\builder\configuration\descriptives\DescriptivesBuilder.java
	final String projectJavaCode = System.getProperty("user.dir").concat("\\");// "D:\\source\\project\\src\\main\\java\\model\\";
	final String methodPrefix = "with";
	final String builderMethodName = "build";

	@Test
	public void createBuilders() throws FileNotFoundException {

		this.generate(this.classes);

	}

	private void generate(final List<Class> classes) throws FileNotFoundException {

		for (final Class clazz : classes) {

			final String builderName = clazz.getSimpleName() + "Builder";
			final String classNameLowerFistLetter = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
			final String packg = clazz.getPackage().getName();
			String text = "package " + packg + ";\n";
			text = text + "public class " + builderName + "{" + "\n";
			text = text + "private final " + clazz.getSimpleName() + " " + classNameLowerFistLetter + ";" + "\n";
			text = text + "public " + builderName + "(){\nthis." + classNameLowerFistLetter + " = new " + clazz.getSimpleName() + "(); \n}" + "\n";

			text = text + Arrays.asList(clazz.getDeclaredFields()).stream().filter(s -> !s.getName().contains("serialVersionUID")).map(f -> {
				final String fieldNameLowerFirstLetter = f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);

				final String generic = this.generatePostFixForGenericTypes(f);

				final String methodName = this.findMethodName(clazz, fieldNameLowerFirstLetter);

				return this.generaterBuilderMethod(builderName, this.methodPrefix, fieldNameLowerFirstLetter, classNameLowerFistLetter, generic,
						methodName, f);
			}).collect(Collectors.joining("\n"));

			text = text + "public " + clazz.getSimpleName() + " " + this.builderMethodName + "(){\n return this." + classNameLowerFistLetter
					+ ";\n}\n}" + "\n";
			final String fileLocation = this.projectJavaCode.concat("src\\main\\java\\")
					.concat(packg.replace(".", "\\")//
							// ##############################
							// ##############################
							// ##############################
							// ##############################
							// Check here for folder location
							// it is the same as package,
							// just replacing the "domain"
							// with "builder" in path
							// ##############################
							// ##############################
							.replace("domain", "persistence")//
							// ##############################
							.concat("\\"))
					+ builderName + ".java";
			System.out.println("Saving Builder under: \n" + fileLocation);
			try (PrintStream out = new PrintStream(new FileOutputStream(fileLocation))) {
				out.print(text);
			}
		}
	}

	private String generatePostFixForGenericTypes(final Field f) {
		String generic = "";
		String separator = "";
		if (f.getGenericType() instanceof ParameterizedType) {
			generic = "<";
			final ParameterizedType aType = (ParameterizedType) f.getGenericType();
			final Type[] fieldArgTypes = aType.getActualTypeArguments();
			for (final Type fieldArgType : fieldArgTypes) {
				try {
					final Class fieldArgClass = (Class) fieldArgType;
					generic = generic + separator + fieldArgClass.getSimpleName();
				} catch (final Exception e) {
					generic = generic + separator + fieldArgType.getTypeName();
				}
				separator = ", ";
			}
			generic = generic + ">";
		}
		return generic;
	}

	private String findMethodName(final Class clazz, final String fieldNameLowerFirstLetter) {
		return Arrays.asList(clazz.getMethods()).stream().map(Method::getName)
				.filter(s -> s.toLowerCase().equals("set" + fieldNameLowerFirstLetter.toLowerCase())
						|| s.toLowerCase().equals("is" + fieldNameLowerFirstLetter.toLowerCase()))
				.findAny().orElse("methodNotFound");
	}

	private String generaterBuilderMethod(final String builderName, final String methodPrefix, final String fieldNameLowerFirstLetter,
			final String classNameLowerFistLetter, final String generic, final String methodName, final Field f) {
		return "public " + builderName + " " + methodPrefix + fieldNameLowerFirstLetter + "(final " + f.getType().getSimpleName() + generic + " "
				+ f.getName() + ") {\n" + "this." + classNameLowerFistLetter + "." + methodName + "(" + f.getName() + ");\n"
				+ "return this;\n" + "}\n";
	}

}
