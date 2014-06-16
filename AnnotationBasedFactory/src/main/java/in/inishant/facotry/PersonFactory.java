package in.inishant.facotry;

import in.inishant.annotations.PersonTypeAnnotation;
import in.inishant.domain.Person;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class PersonFactory {

	private static final Class<PersonTypeAnnotation> PERSON_TYPE_ANNOTATION_CLASS = PersonTypeAnnotation.class;
	String basePackageForOSGI = "/in/inishant/domain";
	String basePackage = "in.inishant.domain";
	Map<String, Class> typeClassMap;

	static public PersonFactory getInstance() {
		return new PersonFactory();
	}

	public PersonFactory() {
		typeClassMap = populateTypeClassMap(getAllPluginClasses());
	}

	protected Map<String, Class> populateTypeClassMap(
			List<String> allPluginClasses) {
		Map<String, Class> typeClassMap = new HashMap<>();
		try {
			for (String string : allPluginClasses) {
				Class cls = Class.forName(string);
				PersonTypeAnnotation an = (PersonTypeAnnotation) cls
						.getAnnotation(PERSON_TYPE_ANNOTATION_CLASS);
				String[] valArr = an.value();
				setClassInTypeClassMap(typeClassMap, cls, valArr);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return typeClassMap;
	}

	/**
	 * @param typeClassMap
	 * @param cls
	 * @param valArr
	 */
	protected void setClassInTypeClassMap(Map<String, Class> typeClassMap,
			Class cls, String[] valArr) {
		for (int i = 0; i < valArr.length; i++) {
			typeClassMap.put(valArr[i], cls);
		}
	}

	public Person getPersonBasedOnDecidingFactor(String decidingFactor) {
		Class perClass = typeClassMap.get(decidingFactor);
		Person per = null;
		if (perClass != null) {
			try {
				per = (Person) perClass.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				// handle excpetion in a better way
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("No such Person");
		}
		return per;
	}

	/**
	 * 
	 */
	protected List<String> getAllPluginClasses() {
		List<String> list = new ArrayList<>();

		// Getting all the class files (Using Spring Beans And Context)
		getAllPluginClassesUsingSpringBeansAndContext(list);

		// Getting all the class files (Using reflections API OF reflections
		// 0.9.9-RC1)
		// getAllPluginClassesUsingReflectionsAPIFromGoogleCode(list);

		// Getting all the class files (also from imported packages)
		// getAllPluginClassesForOSGI(list);

		return list;
	}

	/**
	 * Using Spring
	 * 
	 * @param list
	 */
	protected void getAllPluginClassesUsingSpringBeansAndContext(
			List<String> list) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
				false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(
				PERSON_TYPE_ANNOTATION_CLASS));

		for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
			list.add(bd.getBeanClassName());
		}
	}

	/**
	 * Using Reflections API
	 * 
	 * @param list
	 */
	protected void getAllPluginClassesUsingReflectionsAPIFromGoogleCode(
			List<String> list) {
		Reflections reflections = new Reflections(basePackage);

		Set<Class<?>> annotated = reflections
				.getTypesAnnotatedWith(PERSON_TYPE_ANNOTATION_CLASS);
		for (Class<?> class1 : annotated) {
			list.add(class1.getCanonicalName());
		}
	}

	/**
	 * Using Karaf
	 * 
	 * @param list
	 */
	protected void getAllPluginClassesForOSGI(List<String> list) {
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		BundleWiring wiring = (BundleWiring) bundle.adapt(BundleWiring.class);
		Collection<String> resources = wiring.listResources(basePackageForOSGI,
				"*.class", BundleWiring.LISTRESOURCES_RECURSE);

		List<String> classNamesOfCurrentBundle = new ArrayList<String>();
		for (String resource : resources) {
			String className = resource.replaceAll("/", ".").substring(0,
					resource.length() - 6);
			try {
				Class c = bundle.loadClass(className);
				if (c.isAnnotationPresent(PERSON_TYPE_ANNOTATION_CLASS)) {
					System.out.println(className);
					list.add(className);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PersonFactory pf = PersonFactory.getInstance();
		Person per=pf.getPersonBasedOnDecidingFactor("female");
		System.out.println(per);

	}

}
