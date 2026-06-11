{
  description = "json-insight - A JSON analysis and insights tool";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        # LaTeX subset large enough for pandoc's default xelatex PDF output.
        tex = pkgs.texlive.combine {
          inherit (pkgs.texlive)
            scheme-small
            xetex
            fontspec
            unicode-math
            xcolor
            geometry
            fancyhdr
            booktabs
            ;
        };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            (pkgs.jdk21.override { enableJavaFX = true; })
            maven
            go-task
            pandoc
            tex
          ];

        };
      }
    );
}
