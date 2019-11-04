package de.leonhard.storage.internal.datafiles.config;

import de.leonhard.storage.internal.datafiles.raw.YamlFile;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.Reload;
import de.leonhard.storage.internal.utils.basic.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Extended YamlFile with added methods for Config purposes
 */
@SuppressWarnings("unused")
public class YamlConfig extends YamlFile {

	private List<String> header;


	public YamlConfig(@NotNull final File file, @Nullable final InputStream inputStream, @Nullable final Reload reloadSetting, final boolean preserveComments, @Nullable final DataType dataType) {
		super(file, inputStream, reloadSetting, preserveComments, dataType);
	}


	public List<String> getHeader() {
		if (!this.isPreserveComments()) {
			return new ArrayList<>();
		} else if (!shouldReload()) {
			return header;
		} else {
			try {
				return this.yamlEditor.readHeader();
			} catch (IOException e) {
				System.err.println("Couldn't get header of '" + this.file.getAbsolutePath() + "'.");
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
	}

	public void setHeader(@NotNull final List<String> header) {
		Valid.notNull(header, "Key must not be null");
		List<String> tmp = new ArrayList<>();
		//Updating the values to have a comments, if someone forgets to set them
		for (final String line : header) {
			if (!line.startsWith("#")) {
				tmp.add("#" + line);
			} else {
				tmp.add(line);
			}
		}
		this.header = tmp;

		if (getFile().length() == 0) {
			try {
				this.yamlEditor.write(this.header);
			} catch (IOException e) {
				System.err.println("Error while setting header of '" + this.file.getAbsolutePath() + "'");
				e.printStackTrace();
			}
			return;
		}

		try {
			final List<String> lines = this.yamlEditor.read();
			final List<String> oldHeader = this.yamlEditor.readHeader();

			List<String> newLines = this.header;
			lines.removeAll(oldHeader);
			newLines.addAll(lines);

			this.yamlEditor.write(newLines);
		} catch (final IOException e) {
			System.err.println("Exception while modifying header of '" + this.file.getAbsolutePath() + "'");
			e.printStackTrace();
		}
	}


	protected final YamlConfig getConfigInstance() {
		return this;
	}

	@Override
	public boolean equals(@Nullable final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		} else {
			YamlConfig config = (YamlConfig) obj;
			return this.header.equals(config.header)
				   && super.equals(config.getYamlFileInstance());
		}
	}
}