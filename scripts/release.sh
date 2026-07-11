#!/usr/bin/env bash
set -euo pipefail

VERSION_FILE="${VERSION_FILE:-version.properties}"
GRADLE="${GRADLE:-./gradlew}"
DRY_RUN=0
SKIP_TESTS=0

usage() {
    printf '%s\n' \
        'Usage:' \
        '  scripts/release.sh current' \
        '  scripts/release.sh version-code <x.y.z>' \
        '  scripts/release.sh bump <x.y.z> [--dry-run]' \
        '  scripts/release.sh release <x.y.z> [--dry-run] [--skip-tests]' \
        '  scripts/release.sh tag <x.y.z> [--dry-run]'
}

print_command() {
    printf '+'
    for arg in "$@"; do
        printf ' %q' "$arg"
    done
    printf '\n'
}

run_command() {
    print_command "$@"
    if [[ "$DRY_RUN" == "1" ]]; then
        return 0
    fi
    "$@"
}

require_version_file() {
    if [[ ! -f "$VERSION_FILE" ]]; then
        printf 'Version file not found: %s\n' "$VERSION_FILE" >&2
        exit 1
    fi
}

validate_version() {
    local version="$1"
    if [[ ! "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        printf 'Invalid version: %s\n' "$version" >&2
        printf 'Expected semantic version format: x.y.z\n' >&2
        exit 1
    fi
}

version_code_for() {
    local version="$1"
    validate_version "$version"
    local major minor patch
    IFS='.' read -r major minor patch <<< "$version"
    printf '%d\n' $((major * 1000000 + minor * 1000 + patch))
}

current_version_name() {
    grep -E '^VERSION_NAME=' "$VERSION_FILE" | head -n1 | cut -d'=' -f2-
}

current_version_code() {
    grep -E '^VERSION_CODE=' "$VERSION_FILE" | head -n1 | cut -d'=' -f2-
}

update_versions() {
    local version="$1"
    local version_code="$2"

    if [[ "$DRY_RUN" == "1" ]]; then
        printf 'Would update %s\n' "$VERSION_FILE"
        printf '  versionName: %s -> %s\n' "$(current_version_name)" "$version"
        printf '  versionCode: %s -> %s\n' "$(current_version_code)" "$version_code"
        return 0
    fi

    printf 'VERSION_NAME=%s\nVERSION_CODE=%s\n' "$version" "$version_code" > "$VERSION_FILE"
}

run_release() {
    local version="$1"
    local version_code="$2"

    update_versions "$version" "$version_code"

    if [[ "$SKIP_TESTS" != "1" ]]; then
        run_command "$GRADLE" testDebugUnitTest
    fi

    run_command "$GRADLE" assembleRelease
}

create_git_tag() {
    local version="$1"
    local tag_name="v${version}"
    run_command git tag -a "$tag_name" -m "Release ${tag_name}"
}

main() {
    require_version_file

    local command="${1:-}"
    if [[ -z "$command" ]]; then
        usage
        exit 1
    fi
    shift || true

    case "$command" in
        current)
            printf 'versionName=%s\n' "$(current_version_name)"
            printf 'versionCode=%s\n' "$(current_version_code)"
            ;;
        version-code)
            local version="${1:-}"
            if [[ -z "$version" ]]; then
                usage
                exit 1
            fi
            version_code_for "$version"
            ;;
        bump|release|tag)
            local version="${1:-}"
            if [[ -z "$version" ]]; then
                usage
                exit 1
            fi
            shift || true

            while [[ $# -gt 0 ]]; do
                case "$1" in
                    --dry-run)
                        DRY_RUN=1
                        ;;
                    --skip-tests)
                        SKIP_TESTS=1
                        ;;
                    *)
                        printf 'Unknown option: %s\n' "$1" >&2
                        usage
                        exit 1
                        ;;
                esac
                shift
            done

            local version_code
            version_code="$(version_code_for "$version")"

            if [[ "$command" == "bump" ]]; then
                update_versions "$version" "$version_code"
            elif [[ "$command" == "release" ]]; then
                run_release "$version" "$version_code"
            else
                create_git_tag "$version"
            fi
            ;;
        *)
            printf 'Unknown command: %s\n' "$command" >&2
            usage
            exit 1
            ;;
    esac
}

main "$@"
