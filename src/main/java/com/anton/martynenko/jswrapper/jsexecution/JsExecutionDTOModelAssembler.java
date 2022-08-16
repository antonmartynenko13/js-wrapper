package com.anton.martynenko.jswrapper.jsexecution;

import com.anton.martynenko.jswrapper.jsexecution.constants.Property;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * RepresentationModelAssembler bean. Converts {@link JsExecutionDTO} object into HATEOAS {@link EntityModel} containing links.
 * @author Martynenko Anton
 * @since 1.2
 */

@Component
public class JsExecutionDTOModelAssembler implements RepresentationModelAssembler<JsExecutionDTO, EntityModel<JsExecutionDTO>> {

  @Override
  @NotNull
  public EntityModel<JsExecutionDTO> toModel(@NotNull final JsExecutionDTO jsExecutionDTO) {

    List<Link> links = new ArrayList<>();

    links.add(linkTo(methodOn(JsExecutionController.class).getOne(jsExecutionDTO.getId())).withSelfRel());
    links.add(linkTo(methodOn(JsExecutionController.class).listAll(null, null)).withRel("jsExecutions"));
    links.add(linkTo(methodOn(JsExecutionController.class).deleteJsExecution(jsExecutionDTO.getId())).withRel("delete"));

    if (jsExecutionDTO.isCancellable()) {
      Link link = linkTo(methodOn(JsExecutionController.class)
          .cancelJsExecution(jsExecutionDTO.getId())).withRel("cancel");
      links.add(link);
    }

    Link link = linkTo(methodOn(JsExecutionController.class)
        .getDetails(jsExecutionDTO.getId(), Property.SCRIPT_BODY)).withRel(Property.SCRIPT_BODY);
    links.add(link);

    if (!jsExecutionDTO.getExceptionInfo().isEmpty()) {
      links.add(linkTo(methodOn(JsExecutionController.class)
          .getDetails(jsExecutionDTO.getId(), Property.EXCEPTION_INFO)).withRel(Property.EXCEPTION_INFO));
    }

    if (!jsExecutionDTO.getExecutionLog().isEmpty()) {
      links.add(linkTo(methodOn(JsExecutionController.class)
          .getDetails(jsExecutionDTO.getId(), Property.EXECUTION_LOG)).withRel(Property.EXECUTION_LOG));
    }

    if (!jsExecutionDTO.getErrorLog().isEmpty()) {
      links.add(linkTo(methodOn(JsExecutionController.class)
          .getDetails(jsExecutionDTO.getId(), Property.ERROR_LOG)).withRel(Property.ERROR_LOG));
    }
    return EntityModel.of(jsExecutionDTO, links);
  }
}
