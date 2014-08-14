ANDROID := $(ANDROID_HOME)/tools/android
ADB := $(ANDROID_HOME)/platform-tools/adb

API := android-19

ANDROIDAPIS := $(API) # Add others as required.

ANDROIDSOURCES := $(addprefix $(ANDROID_HOME)/sources/,$(ANDROIDAPIS))

PACKAGE := com.example.android.sunshine

# Task names extracted from output of './gradlew tasks' and re-ordered
# slightly.
#
GRADLETASKS :=\
	help\
	tasks\
	androidDependencies\
	signingReport\
	assemble\
	assembleDebug\
	assembleDebugTest\
	assembleRelease\
	build\
	buildDependents\
	buildNeeded\
	clean\
	init\
	wrapper\
	dependencies\
	dependencyInsight\
	projects\
	properties\
	installDebug\
	installDebugTest\
	uninstallAll\
	uninstallDebug\
	uninstallDebugTest\
	uninstallRelease\
	check\
	connectedAndroidTest\
	connectedCheck\
	deviceCheck\
	#


$(GRADLETASKS):
	./gradlew $@


debug: installDebug
	$(ADB) logcat -c
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	$(ADB) logcat


lint lintDebug lintRelease:
	./gradlew $@
	open app/build/outputs/lint-results.html
.PHONY: lint lintDebug lintRelease


define TAGJAVA
find $(1) -type f -name '*.java' -print |\
xargs etags --append --output=$(2) &&
endef
TAGS tags: . $(LIBRARIES) $(ANDROIDSOURCES)
	rm -f TAGS.tmp
	$(foreach d,$^,$(call TAGJAVA,$(d),TAGS.tmp)) true
	cmp -s ./TAGS TAGS.tmp || rm -f ./TAGS && mv TAGS.tmp ./TAGS
.PHONY: TAGS tags


distclean: clean
	rm -f TAGS
