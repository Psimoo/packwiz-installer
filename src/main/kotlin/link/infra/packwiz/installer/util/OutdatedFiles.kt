package link.infra.packwiz.installer.util

import link.infra.packwiz.installer.target.path.PackwizFilePath
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Files that are no longer part of the pack (removed from the index, disabled, or for the other
 * side) used to be deleted. Instead, they are moved into the packwiz_Outdated folder inside the
 * pack folder, so that nothing is ever lost.
 */
object OutdatedFiles {
	const val FOLDER_NAME = "packwiz_Outdated"

	/** The packwiz_Outdated folder inside the pack folder (it is only created when it is needed) */
	fun folder(packFolder: PackwizFilePath): Path =
		packFolder.nioPath.toAbsolutePath().normalize().resolve(FOLDER_NAME)

	/**
	 * Moves [file] into the packwiz_Outdated folder in [packFolder], keeping its path relative to
	 * the pack folder (e.g. mods/foo-1.0.jar becomes packwiz_Outdated/mods/foo-1.0.jar).
	 * If a file with the same name already exists there, a number is appended to the name.
	 *
	 * @return true if the file is now in the outdated folder (or didn't exist in the first place)
	 */
	fun moveToOutdated(packFolder: PackwizFilePath, file: Path): Boolean {
		val source = file.toAbsolutePath().normalize()
		if (!Files.exists(source)) {
			// Nothing to move - the file was already gone
			return true
		}

		val packRoot = packFolder.nioPath.toAbsolutePath().normalize()
		val outdatedRoot = packRoot.resolve(FOLDER_NAME)
		if (source.startsWith(outdatedRoot)) {
			// Already in the outdated folder, leave it alone
			return true
		}

		// Keep the folder structure, so that mods/foo.jar and config/foo.jar don't clash
		val relative = if (source.startsWith(packRoot)) packRoot.relativize(source) else source.fileName

		try {
			val dest = moveToUniqueDestination(source, outdatedRoot.resolve(relative))
			Log.info("Moved $source to $FOLDER_NAME ($dest)")
			return true
		} catch (e: Exception) {
			Log.warn("Failed to move $source to the $FOLDER_NAME folder (it is still present, but is no longer managed by packwiz)", e)
			return false
		}
	}

	/** Moves [source] to [dest], appending a number to the file name if it is already taken. */
	private fun moveToUniqueDestination(source: Path, dest: Path): Path {
		Files.createDirectories(dest.parent)
		var candidate = dest
		// A limited number of attempts, just in case something is very wrong
		for (i in 0..99) {
			try {
				Files.move(source, candidate)
				return candidate
			} catch (e: FileAlreadyExistsException) {
				// Another file (usually an older version of the same mod) is already there
			} catch (e: Exception) {
				// Moving can fail if the file is locked (Windows), so fall back to copy + delete
				Files.copy(source, candidate, StandardCopyOption.REPLACE_EXISTING)
				Files.deleteIfExists(source)
				return candidate
			}
			candidate = numberedSibling(dest, i + 1)
		}
		throw IllegalStateException("Could not find a free name for $dest")
	}

	/** "mods/foo.jar" -> "mods/foo (1).jar" */
	private fun numberedSibling(dest: Path, number: Int): Path {
		val name = dest.fileName.toString()
		val dot = name.lastIndexOf('.')
		return if (dot > 0) {
			dest.resolveSibling(name.substring(0, dot) + " (" + number + ")" + name.substring(dot))
		} else {
			dest.resolveSibling(name + " (" + number + ")")
		}
	}
}
