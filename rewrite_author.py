def commit_callback(commit):
    if commit.author_name == b"Jaydan":
        commit.author_name = b"scoutcraft218"
    if commit.author_email == b"old.email@example.com":
        commit.author_email = b"new.email@example.com"
    if commit.committer_name == b"Jaydan":
        commit.committer_name = b"scoutcraft218"
    if commit.committer_email == b"old.email@example.com":
        commit.committer_email = b"new.email@example.com"